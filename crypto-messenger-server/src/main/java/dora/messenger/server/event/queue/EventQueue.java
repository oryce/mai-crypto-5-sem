package dora.messenger.server.event.queue;

import java.io.IOException;
import java.util.function.Consumer;

public interface EventQueue {

    /**
     * Publishes an event to queue <code>queueName</code>.
     */
    void publish(String queueName, Envelope event) throws IOException;

    /**
     * Subscribes to deliveries in queue <code>queueName.</code>
     */
    Subscription subscribe(String queueName, Consumer<Delivery> consumer) throws IOException;
}
