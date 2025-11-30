package dora.messenger.server.contact;

import dora.messenger.protocol.contact.ContactAddedEventDto;
import dora.messenger.protocol.contact.ContactDto;
import dora.messenger.server.event.Event;
import dora.messenger.server.user.User;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

public record ContactAddedEvent(Contact contact, User otherUser) implements Event {

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING, uses = { User.Mapper.class })
    public interface Mapper {

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "contact", source = "event")
        ContactAddedEventDto toDto(ContactAddedEvent event);

        // FIXME (17.12.25, ~oryce):
        //   Use accessors in `ContactDto` to remove this mapper.

        @Mapping(target = "id", source = "contact.id")
        @Mapping(target = "user", source = "otherUser")
        ContactDto contactToDto(ContactAddedEvent event);
    }
}
