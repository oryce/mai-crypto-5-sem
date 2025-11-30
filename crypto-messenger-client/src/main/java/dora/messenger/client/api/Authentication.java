package dora.messenger.client.api;

import feign.RequestTemplate;
import org.java_websocket.client.WebSocketClient;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpRequest;
import java.util.Objects;

public interface Authentication {

    void applyToFeign(RequestTemplate request);

    void applyToJava(HttpRequest.Builder builder);

    void applyToWebSocket(WebSocketClient client);

    static Authentication bearer(@NotNull String token) {
        return new Bearer(token);
    }

    record Bearer(@NotNull String token) implements Authentication {

        public Bearer {
            Objects.requireNonNull(token, "token");
        }

        @Override
        public void applyToFeign(RequestTemplate request) {
            request.header("Authorization", "Bearer " + token);
        }

        @Override
        public void applyToJava(HttpRequest.Builder builder) {
            builder.header("Authorization", "Bearer " + token);
        }

        @Override
        public void applyToWebSocket(WebSocketClient client) {
            client.addHeader("Authorization", "Bearer " + token);
        }
    }
}
