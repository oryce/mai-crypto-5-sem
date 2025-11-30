package dora.messenger.server.event.websocket;

import dora.messenger.server.session.Session;
import dora.messenger.server.session.SessionService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class SessionHandshakeInterceptor implements HandshakeInterceptor {

    private static final String SCHEME = "Bearer";

    private final SessionService sessionService;

    public SessionHandshakeInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean beforeHandshake(
        @NonNull ServerHttpRequest request,
        @NonNull ServerHttpResponse response,
        @NonNull WebSocketHandler wsHandler,
        @NonNull Map<String, Object> attributes
    ) {
        String accessToken = parseAuthorization(request);
        if (accessToken == null) return false;

        Session session;
        try {
            session = sessionService.validateAccessToken(accessToken);
        } catch (Exception e) {
            return false;
        }

        attributes.put("session", session);
        attributes.put("user", session.getUser());
        return true;
    }

    private @Nullable String parseAuthorization(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null) return null;

        String[] parts = authorization.split(" ");
        if (parts.length != 2 || !parts[0].equals(SCHEME)) return null;

        return parts[1];
    }

    @Override
    public void afterHandshake(
        @NonNull ServerHttpRequest request,
        @NonNull ServerHttpResponse response,
        @NonNull WebSocketHandler wsHandler,
        @Nullable Exception exception
    ) {
    }
}
