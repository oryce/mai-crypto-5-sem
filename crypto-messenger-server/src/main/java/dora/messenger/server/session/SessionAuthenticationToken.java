package dora.messenger.server.session;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;
import java.util.Objects;

public class SessionAuthenticationToken extends AbstractAuthenticationToken {

    private final Session session;
    private final String accessToken;

    public SessionAuthenticationToken(@NonNull Session session, @NonNull String accessToken) {
        super(Collections.emptyList());

        this.session = Objects.requireNonNull(session, "session");
        this.accessToken = Objects.requireNonNull(accessToken, "access token");

        setAuthenticated(true);
    }

    @Override
    public @Nullable Object getCredentials() {
        return accessToken;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return session.getUser();
    }

    public Session getSession() {
        return session;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
