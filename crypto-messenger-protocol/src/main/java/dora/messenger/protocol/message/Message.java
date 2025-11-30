package dora.messenger.protocol.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = EventMessage.class, name = "EVENT"),
    @JsonSubTypes.Type(value = AcknowledgmentMessage.class, name = "ACKNOWLEDGMENT")
})
public interface Message {

    void accept(Visitor visitor);

    interface Visitor {

        void visitEventMessage(EventMessage message);
        void visitAcknowledgmentMessage(AcknowledgmentMessage message);
    }
}
