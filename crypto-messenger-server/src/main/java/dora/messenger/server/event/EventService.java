package dora.messenger.server.event;

import dora.messenger.server.event.queue.Delivery;
import dora.messenger.server.event.queue.EventQueue;
import dora.messenger.server.event.queue.Envelope;
import dora.messenger.server.event.queue.Subscription;
import dora.messenger.server.user.User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class EventService {

    private final EventQueue eventQueue;

    public EventService(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    public void publish(User user, Event event) {
        try {
            String eventId = UUID.randomUUID().toString();
            eventQueue.publish(queueName(user), new Envelope(eventId, event));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Subscription subscribe(User user, Consumer<Delivery> consumer) {
        try {
            return eventQueue.subscribe(queueName(user), consumer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String queueName(User user) {
        return user.getId().toString();
    }
}
