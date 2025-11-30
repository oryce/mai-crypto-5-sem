package dora.messenger.protocol.message;

import dora.messenger.protocol.EventDto;

/**
 * @param event event payload
 */
public record EventMessage(
    EventDto event
) implements Message {

    @Override
    public void accept(Visitor visitor) {
        visitor.visitEventMessage(this);
    }
}
