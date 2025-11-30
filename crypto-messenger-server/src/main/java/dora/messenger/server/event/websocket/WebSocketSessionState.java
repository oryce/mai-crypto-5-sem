package dora.messenger.server.event.websocket;

import dora.messenger.server.event.queue.Delivery;
import dora.messenger.server.event.queue.Subscription;
import dora.messenger.server.user.User;

import java.util.Map;

/**
 * @param user           authenticated user
 * @param unacknowledged unacknowledged events
 * @param subscription   event queue subscription
 */
public record WebSocketSessionState(
    User user,
    Map<String, Delivery> unacknowledged,
    Subscription subscription
) {
}
