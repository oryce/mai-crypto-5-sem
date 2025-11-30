package dora.messenger.server.session;

import dora.messenger.server.user.User;
import dora.messenger.server.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.Instant;

@Service
@Validated
public class SessionService {

    private final UserRepository users;
    private final SessionRepository sessions;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final Duration lifetime;

    public SessionService(
        UserRepository users,
        SessionRepository sessions,
        PasswordEncoder passwordEncoder,
        TokenIssuer tokenIssuer,
        @Qualifier("sessionLifetime") Duration lifetime
    ) {
        this.users = users;
        this.sessions = sessions;
        this.passwordEncoder = passwordEncoder;
        this.tokenIssuer = tokenIssuer;
        this.lifetime = lifetime;
    }

    public Session createSession(
        @NotBlank(message = "Username may not be blank")
        String username,
        @NotBlank(message = "Password may not be blank")
        String password
    ) {
        User user = users.findByUsername(username)
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return createSession(user);
    }

    public Session createSession(@NotNull User user) {
        Session session = new Session();
        Instant now = Instant.now();

        session.setUser(user);
        session.setCreatedAt(now);
        session.setExpiresAt(now.plus(lifetime));

        return sessions.save(session);
    }

    public SessionCredentials createCredentials(Session session) {
        String accessToken = tokenIssuer.createToken(TokenIssuer.Metadata.of(session));
        return new SessionCredentials(accessToken);
    }

    public Session validateAccessToken(String accessToken) {
        TokenIssuer.Metadata metadata;

        try {
            metadata = tokenIssuer.validateToken(accessToken);
        } catch (TokenIssuer.InvalidTokenException e) {
            throw new InvalidAccessTokenException();
        }

        Session session = sessions.findById(metadata.sessionId())
            .orElseThrow(InvalidAccessTokenException::new);

        if (session.isExpired()) {
            throw new ExpiredSessionException();
        }

        return session;
    }

    public void refreshSession(Session session) {
        session.setExpiresAt(Instant.now().plus(lifetime));
        sessions.save(session);
    }

    public void invalidateSession(Session session) {
        sessions.delete(session);
    }

    public static final class InvalidCredentialsException extends RuntimeException {

        public InvalidCredentialsException() {
            super("Invalid username or password");
        }
    }

    public static final class InvalidAccessTokenException extends RuntimeException {

        public InvalidAccessTokenException() {
            super("Invalid access token");
        }
    }

    public static final class ExpiredSessionException extends RuntimeException {

        public ExpiredSessionException() {
            super("Session has expired");
        }
    }
}
