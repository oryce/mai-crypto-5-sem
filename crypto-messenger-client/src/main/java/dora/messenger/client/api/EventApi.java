package dora.messenger.client.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dora.messenger.protocol.EventDto;
import dora.messenger.protocol.message.AcknowledgmentMessage;
import dora.messenger.protocol.message.EventMessage;
import dora.messenger.protocol.message.Message;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Singleton
public class EventApi {

    private final ObjectMapper objectMapper;
    private final AuthenticationProvider authenticationProvider;
    private final List<Subscriber> subscribers;

    private Client client;

    @Inject
    public EventApi(
        @NotNull ObjectMapper objectMapper,
        @NotNull AuthenticationProvider authenticationProvider
    ) {
        this.objectMapper = requireNonNull(objectMapper, "object mapper");
        this.authenticationProvider = requireNonNull(authenticationProvider, "authentication provider");
        this.subscribers = new CopyOnWriteArrayList<>();
    }

    public void connect() {
        // TODO (21.12.25, ~oryce):
        //   Don't hardcode base URI.
        URI endpoint = URI.create("ws://localhost:8080/events");

        client = new Client(endpoint);
        client.connect();
    }

    public void disconnect() {
        client.close();
        client = null;
    }

    public <T extends EventDto> Subscription subscribe(Class<T> event, Consumer<T> consumer) {
        Subscriber subscriber = new Subscriber(event, consumer);
        subscribers.add(subscriber);
        return () -> subscribers.remove(subscriber);
    }

    public interface Subscription {

        void unsubscribe();
    }

    private <T extends EventDto> Collection<Subscriber> getSubscribers(Class<T> event) {
        return subscribers.stream()
            .filter((subscriber) -> subscriber.event() == event)
            .toList();
    }

    private class Client extends WebSocketClient implements Message.Visitor {

        private static final Logger LOGGER = LogManager.getLogger(Client.class);

        public Client(URI serverUri) {
            super(serverUri);

            authenticationProvider.authentication().ifPresent((authentication) ->
                authentication.applyToWebSocket(this)
            );
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            LOGGER.debug("WebSocket connection established");
        }

        @Override
        public void onMessage(String serializedMessage) {
            Message message = readMessage(serializedMessage);
            if (message != null) message.accept(this);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            LOGGER.debug("WebSocket connection closed (code={}, reason={}))", code, reason);
        }

        @Override
        public void onError(Exception exception) {
            LOGGER.error("Exception occurred in WebSocket connection", exception);
        }

        @Override
        public void visitEventMessage(EventMessage message) {
            EventDto event = message.event();

            // Notify subscribers.
            Collection<Subscriber> subscribers = getSubscribers(event.getClass());
            subscribers.forEach((subscriber) -> {
                try {
                    //noinspection rawtypes,unchecked: The cast is safe because event types match.
                    ((Consumer) subscriber.consumer()).accept(event);
                } catch (Exception e) {
                    LOGGER.error("Exception occurred in event consumer", e);
                }
            });

            // Acknowledge event.
            AcknowledgmentMessage acknowledgment = new AcknowledgmentMessage(event.getId());
            sendMessage(acknowledgment);
        }

        @Override
        public void visitAcknowledgmentMessage(AcknowledgmentMessage message) {
            // Server does not send event acknowledgments.
            throw new UnsupportedOperationException();
        }

        private @Nullable Message readMessage(String serializedMessage) {
            try {
                return objectMapper.readValue(serializedMessage, Message.class);
            } catch (JsonProcessingException e) {
                LOGGER.error("Cannot read WebSocket message", e);
                return null;
            }
        }

        private void sendMessage(Message message) {
            try {
                String serializedMessage = objectMapper.writeValueAsString(message);
                send(serializedMessage);
            } catch (JsonProcessingException e) {
                LOGGER.error("Cannot serialize WebSocket message", e);
            }
        }
    }

    private record Subscriber(
        Class<? extends EventDto> event,
        Consumer<? extends EventDto> consumer
    ) {
    }
}
