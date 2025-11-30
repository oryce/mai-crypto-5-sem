package dora.messenger.server.session;

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

    class InvalidTokenException extends Exception {

        public InvalidTokenException(String message) {
            super(message);
        }

        public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
