package dora.messenger.server.chat;

import dora.messenger.protocol.chat.ChatRemovedEventDto;
import dora.messenger.server.event.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.UUID;

public record ChatRemovedEvent(UUID chatId) implements Event {

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING)
    public interface Mapper {

        @Mapping(target = "id", ignore = true)
        ChatRemovedEventDto toDto(ChatRemovedEvent event);
    }
}
