package dora.messenger.server.contact;

import dora.messenger.protocol.contact.ContactRemovedEventDto;
import dora.messenger.server.event.Event;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

public record ContactRemovedEvent(Contact contact) implements Event {

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING)
    public interface Mapper {

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "contactId", source = "contact.id")
        ContactRemovedEventDto toDto(ContactRemovedEvent event);
    }
}
