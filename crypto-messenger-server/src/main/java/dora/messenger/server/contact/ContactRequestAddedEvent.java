package dora.messenger.server.contact;

import dora.messenger.protocol.contact.ContactRequestDto;
import dora.messenger.protocol.contact.ContactRequestAddedEventDto;
import dora.messenger.server.event.Event;
import dora.messenger.server.user.User;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

public record ContactRequestAddedEvent(ContactRequest request) implements Event {

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING, uses = { User.Mapper.class })
    public interface Mapper {

        @Mapping(target = "id", ignore = true)
        ContactRequestAddedEventDto toDto(ContactRequestAddedEvent event);

        // Contact request events are only sent to the responding user.

        @Mapping(target = "direction", constant = "INCOMING")
        @Mapping(target = "user", source = "initiator")
        ContactRequestDto toDto(ContactRequest request);
    }
}
