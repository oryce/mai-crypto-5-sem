package dora.messenger.server.event.queue;

public interface Delivery {

    /**
     * Returns the delivered event.
     */
    Envelope envelope();

    /**
     * Acknowledges the delivery.
     */
    void acknowledge();
}
