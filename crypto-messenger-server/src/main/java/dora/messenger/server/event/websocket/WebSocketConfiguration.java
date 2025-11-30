package dora.messenger.server.event.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final EventsWebSocketHandler eventsHandler;
    private final SessionHandshakeInterceptor sessionHandshakeInterceptor;

    public WebSocketConfiguration(
        EventsWebSocketHandler eventsHandler,
        SessionHandshakeInterceptor sessionHandshakeInterceptor
    ) {
        this.eventsHandler = eventsHandler;
        this.sessionHandshakeInterceptor = sessionHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(eventsHandler, "/events")
            .addInterceptors(sessionHandshakeInterceptor);
    }
}
