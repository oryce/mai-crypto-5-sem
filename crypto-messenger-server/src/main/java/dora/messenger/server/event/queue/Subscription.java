package dora.messenger.server.event.queue;

public interface Subscription {

    /**
     * Unsubscribes from deliveries.
     */
    void unsubscribe();
}
