package dora.messenger.protocol.message;

/**
 * @param eventId acknowledged event ID
 */
public record AcknowledgmentMessage(
    String eventId
) implements Message {

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAcknowledgmentMessage(this);
    }
}
