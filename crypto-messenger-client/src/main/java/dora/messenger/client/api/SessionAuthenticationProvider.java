package dora.messenger.client.api;

import dora.messenger.client.store.session.SessionCredentials;
import dora.messenger.client.store.session.SessionStore;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class SessionAuthenticationProvider implements AuthenticationProvider {

    private final Provider<SessionStore> storeProvider;

    @Inject
    public SessionAuthenticationProvider(@NotNull Provider<SessionStore> storeProvider) {
        this.storeProvider = Objects.requireNonNull(storeProvider, "store provider");
    }

    @Override
    public Optional<Authentication> authentication() {
        SessionStore store = storeProvider.get();
        SessionCredentials credentials = store.getCredentials().get();

        return Optional.ofNullable(credentials)
            .map((creds) -> Authentication.bearer(creds.accessToken()));
    }
}
