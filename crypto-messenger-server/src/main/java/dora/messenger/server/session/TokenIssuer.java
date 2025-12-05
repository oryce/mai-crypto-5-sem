package dora.messenger.server.session;

import lombok.experimental.StandardException;

import java.time.Instant;
import java.util.UUID;

public interface TokenIssuer {

    String createToken(Metadata metadata);

    Metadata validateToken(String token) throws InvalidTokenException;

    record Metadata(UUID sessionId, Instant createdAt) {

        public static Metadata of(Session session) {
            return new Metadata(session.getId(), session.getCreatedAt());
        }
    }

    @StandardException
    class InvalidTokenException extends Exception {
    }
}
