package dora.messenger.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dora.messenger.protocol.chat.ChatAddedEventDto;
import dora.messenger.protocol.chat.ChatRemovedEventDto;
import dora.messenger.protocol.chat.message.ChatMessageReceivedEventDto;
import dora.messenger.protocol.chat.session.ChatSessionInitiationEventDto;
import dora.messenger.protocol.chat.session.ChatSessionRemovedEventDto;
import dora.messenger.protocol.chat.session.ChatSessionResponseEventDto;
import dora.messenger.protocol.contact.ContactAddedEventDto;
import dora.messenger.protocol.contact.ContactRemovedEventDto;
import dora.messenger.protocol.contact.ContactRequestAddedEventDto;
import dora.messenger.protocol.contact.ContactRequestRemovedEventDto;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "name"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ContactRequestAddedEventDto.class, name = "CONTACT_REQUEST_ADDED"),
    @JsonSubTypes.Type(value = ContactRequestRemovedEventDto.class, name = "CONTACT_REQUEST_REMOVED"),
    @JsonSubTypes.Type(value = ContactAddedEventDto.class, name = "CONTACT_ADDED"),
    @JsonSubTypes.Type(value = ContactRemovedEventDto.class, name = "CONTACT_REMOVED"),
    @JsonSubTypes.Type(value = ChatAddedEventDto.class, name = "CHAT_ADDED"),
    @JsonSubTypes.Type(value = ChatRemovedEventDto.class, name = "CHAT_REMOVED"),
    @JsonSubTypes.Type(value = ChatSessionInitiationEventDto.class, name = "CHAT_SESSION_INITIATION"),
    @JsonSubTypes.Type(value = ChatSessionResponseEventDto.class, name = "CHAT_SESSION_RESPONSE"),
    @JsonSubTypes.Type(value = ChatSessionRemovedEventDto.class, name = "CHAT_SESSION_REMOVED"),
    @JsonSubTypes.Type(value = ChatMessageReceivedEventDto.class, name = "CHAT_MESSAGE_RECEIVED")
})
public abstract class EventDto {

    private String id;

    //region Accessors
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    //endregion
}
