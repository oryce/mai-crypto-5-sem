package dora.messenger.server.session;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record SessionCredentials(@NonNull String accessToken) {

    public SessionCredentials {
        Objects.requireNonNull(accessToken, "access token");
    }
}
