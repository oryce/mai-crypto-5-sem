package dora.messenger.server.event.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dora.messenger.protocol.EventDto;
import dora.messenger.protocol.message.AcknowledgmentMessage;
import dora.messenger.protocol.message.EventMessage;
import dora.messenger.protocol.message.Message;
import dora.messenger.server.event.Event;
import dora.messenger.server.event.EventService;
import dora.messenger.server.event.queue.Delivery;
import dora.messenger.server.event.queue.Envelope;
import dora.messenger.server.event.queue.Subscription;
import dora.messenger.server.user.User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventsWebSocketHandler extends TextWebSocketHandler {

    private final EventService eventService;
    private final ObjectMapper objectMapper;
    private final Event.Mapper eventMapper;
    private final Map<WebSocketSession, WebSocketSessionState> connections;

    public EventsWebSocketHandler(
        EventService eventService,
        ObjectMapper objectMapper,
        Event.Mapper eventMapper
    ) {
        this.eventService = eventService;
        this.objectMapper = objectMapper;
        this.eventMapper = eventMapper;
        this.connections = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        User user = (User) session.getAttributes().get("user");
        Map<String, Delivery> unacknowledged = new ConcurrentHashMap<>();

        // FIXME (17.12.25, ~oryce):
        //   Potential concurrency hazard (sending messages from Rabbit thread.)

        Subscription subscription = eventService.subscribe(user, (delivery) -> {
            Envelope envelope = delivery.envelope();

            EventDto eventDto = eventMapper.toDto(envelope.event(), envelope.id());
            try {
                sendMessage(session, new EventMessage(eventDto));
            } catch (IOException e) {
                // TODO (17.12.25, ~oryce):
                //   Handle exception.
                return;
            }

            unacknowledged.put(envelope.id(), delivery);
        });

        connections.put(session, new WebSocketSessionState(user, unacknowledged, subscription));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        WebSocketSessionState state = connections.remove(session);
        if (state == null) return;

        // FIXME (17.12.25, ~oryce):
        //   Concurrency hazard (if called from another thread.)

        state.subscription().unsubscribe();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        WebSocketSessionState state = connections.get(session);
        Message message = objectMapper.readValue(textMessage.getPayload(), Message.class);

        if (message instanceof AcknowledgmentMessage(String eventId)) {
            Delivery delivery = state.unacknowledged().remove(eventId);

            // FIXME (17.12.25, ~oryce):
            //   Concurrency hazard (if called from another thread.)

            if (delivery != null) delivery.acknowledge();
        }
    }

    private void sendMessage(WebSocketSession session, Message message) throws IOException {
        String serializedMessage = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(serializedMessage));
    }
}
