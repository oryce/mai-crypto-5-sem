package dora.messenger.server.event.queue;

import dora.messenger.server.event.Event;

public record Envelope(String id, Event event) {
}
