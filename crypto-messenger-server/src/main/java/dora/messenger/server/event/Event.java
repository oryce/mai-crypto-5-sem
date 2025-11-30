package dora.messenger.server.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dora.messenger.protocol.EventDto;
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
import dora.messenger.server.chat.ChatAddedEvent;
import dora.messenger.server.chat.ChatRemovedEvent;
import dora.messenger.server.chat.message.ChatMessageReceivedEvent;
import dora.messenger.server.chat.session.ChatSessionInitiationEvent;
import dora.messenger.server.chat.session.ChatSessionRemovedEvent;
import dora.messenger.server.chat.session.ChatSessionResponseEvent;
import dora.messenger.server.contact.ContactAddedEvent;
import dora.messenger.server.contact.ContactRemovedEvent;
import dora.messenger.server.contact.ContactRequestAddedEvent;
import dora.messenger.server.contact.ContactRequestRemovedEvent;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "name"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ContactRequestAddedEvent.class, name = "CONTACT_REQUEST_ADDED"),
    @JsonSubTypes.Type(value = ContactRequestRemovedEvent.class, name = "CONTACT_REQUEST_REMOVED"),
    @JsonSubTypes.Type(value = ContactAddedEvent.class, name = "CONTACT_ADDED"),
    @JsonSubTypes.Type(value = ContactRemovedEvent.class, name = "CONTACT_REMOVED"),
    @JsonSubTypes.Type(value = ChatAddedEvent.class, name = "CHAT_ADDED"),
    @JsonSubTypes.Type(value = ChatRemovedEvent.class, name = "CHAT_REMOVED"),
    @JsonSubTypes.Type(value = ChatSessionInitiationEvent.class, name = "CHAT_SESSION_INITIATION"),
    @JsonSubTypes.Type(value = ChatSessionResponseEvent.class, name = "CHAT_SESSION_RESPONSE"),
    @JsonSubTypes.Type(value = ChatSessionRemovedEvent.class, name = "CHAT_SESSION_REMOVED"),
    @JsonSubTypes.Type(value = ChatMessageReceivedEvent.class, name = "CHAT_MESSAGE_RECEIVED")
})
public interface Event {

    @org.mapstruct.Mapper(
        componentModel = ComponentModel.SPRING,
        subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION,
        uses = {
            ContactRequestAddedEvent.Mapper.class,
            ContactRequestRemovedEvent.Mapper.class,
            ContactAddedEvent.Mapper.class,
            ContactRemovedEvent.Mapper.class,
            ChatAddedEvent.Mapper.class,
            ChatRemovedEvent.Mapper.class,
            ChatSessionInitiationEvent.Mapper.class,
            ChatSessionResponseEvent.Mapper.class,
            ChatSessionRemovedEvent.Mapper.class,
            ChatMessageReceivedEvent.Mapper.class
        }
    )
    interface Mapper {

        @SubclassMapping(target = ContactRequestAddedEventDto.class, source = ContactRequestAddedEvent.class)
        @SubclassMapping(target = ContactRequestRemovedEventDto.class, source = ContactRequestRemovedEvent.class)
        @SubclassMapping(target = ContactAddedEventDto.class, source = ContactAddedEvent.class)
        @SubclassMapping(target = ContactRemovedEventDto.class, source = ContactRemovedEvent.class)
        @SubclassMapping(target = ChatAddedEventDto.class, source = ChatAddedEvent.class)
        @SubclassMapping(target = ChatRemovedEventDto.class, source = ChatRemovedEvent.class)
        @SubclassMapping(target = ChatSessionInitiationEventDto.class, source = ChatSessionInitiationEvent.class)
        @SubclassMapping(target = ChatSessionResponseEventDto.class, source = ChatSessionResponseEvent.class)
        @SubclassMapping(target = ChatSessionRemovedEventDto.class, source = ChatSessionRemovedEvent.class)
        @SubclassMapping(target = ChatMessageReceivedEventDto.class, source = ChatMessageReceivedEvent.class)
        EventDto toDto(Event event);

        default EventDto toDto(Event event, String id) {
            EventDto eventDto = toDto(event);
            eventDto.setId(id);
            return eventDto;
        }
    }
}
