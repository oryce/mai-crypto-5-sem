package dora.messenger.server.contact;

import dora.messenger.protocol.contact.ContactRequestRemovedEventDto;
import dora.messenger.server.event.Event;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

public record ContactRequestRemovedEvent(ContactRequest request) implements Event {

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING)
    public interface Mapper {

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "requestId", source = "request.id")
        ContactRequestRemovedEventDto toDto(ContactRequestRemovedEvent event);
    }
}
