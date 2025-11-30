package dora.messenger.client.store.session;

import dora.messenger.client.api.SessionApi;
import dora.messenger.client.persistence.PropertyKey;
import dora.messenger.client.persistence.PropertyRepository;
import dora.messenger.client.store.Computed;
import dora.messenger.client.store.Observable;
import dora.messenger.protocol.session.CreateSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

@Singleton
public class SessionStore {

    private final SessionApi sessionApi;

    @Inject
    public SessionStore(@NotNull SessionApi sessionApi, @NotNull PropertyRepository properties) {
        this.sessionApi = requireNonNull(sessionApi, "session API");

        credentialsComputed = Computed.of(
            () -> properties.getString(PropertyKey.ACCESS_TOKEN)
                .map(SessionCredentials::new)
                .orElse(null),
            (credentials) -> {
                String accessToken = credentials != null ? credentials.accessToken() : null;
                properties.setString(PropertyKey.ACCESS_TOKEN, accessToken);
            }
        );
    }

    private final Computed<SessionCredentials> credentialsComputed;

    public Computed<SessionCredentials> getCredentials() {
        return credentialsComputed;
    }

    public CompletableFuture<Void> login(@NotNull String username, @NotNull String password) {
        requireNonNull(username, "username");
        requireNonNull(password, "password");

        return sessionApi.createSession(new CreateSession(username, password))
            .thenAccept((createdSession) -> {
                SessionCredentials credentials = SessionCredentials.from(createdSession.credentials());
                credentialsComputed.set(credentials);
            });
    }

    public CompletableFuture<Void> logout() {
        return sessionApi.deleteSession().thenRun(() -> credentialsComputed.set(null));
    }
}
