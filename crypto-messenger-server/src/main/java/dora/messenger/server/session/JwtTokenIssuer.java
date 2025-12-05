package dora.messenger.server.session;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class JwtTokenIssuer implements TokenIssuer {

    private static final String AUDIENCE = "dora-messenger/access-token";
    private static final String SID_CLAIM = "sid";

    private final Algorithm algorithm;

    public JwtTokenIssuer(@Qualifier("sessionJwtAlgorithm") Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String createToken(Metadata metadata) {
        return JWT.create()
            .withAudience(AUDIENCE)
            .withIssuedAt(metadata.createdAt())
            .withNotBefore(metadata.createdAt())
            .withClaim(SID_CLAIM, metadata.sessionId().toString())
            .sign(algorithm);
    }

    @Override
    public Metadata validateToken(String token) throws InvalidTokenException {
        JWTVerifier verifier = JWT.require(algorithm)
            .withAudience(AUDIENCE)
            .build();

        DecodedJWT jwt;
        try {
            jwt = verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new InvalidTokenException("Cannot verify token", e);
        }

        try {
            UUID sessionId = UUID.fromString(jwt.getClaim(SID_CLAIM).asString());
            Instant createdAt = jwt.getIssuedAtAsInstant();

            return new Metadata(sessionId, createdAt);
        } catch (Exception e) {
            throw new InvalidTokenException("Cannot parse token", e);
        }
    }
}
