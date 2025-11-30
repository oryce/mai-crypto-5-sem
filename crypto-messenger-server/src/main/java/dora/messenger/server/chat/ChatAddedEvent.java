package dora.messenger.server.chat;

import dora.messenger.protocol.chat.ChatAddedEventDto;
import dora.messenger.protocol.chat.ChatDto;
import dora.messenger.server.event.Event;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

public record ChatAddedEvent(Chat chat, String name) implements Event {

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING)
    public interface Mapper {

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "chat", source = "event")
        ChatAddedEventDto toDto(ChatAddedEvent event);

        @Mapping(target = "id", source = "chat.id")
        @Mapping(target = "name", source = "name")
        @Mapping(target = "dhGroup", source = "chat.dhGroup")
        @Mapping(target = "algorithm", source = "chat.algorithm")
        @Mapping(target = "cipherMode", source = "chat.cipherMode")
        @Mapping(target = "padding", source = "chat.padding")
        ChatDto chatToDto(ChatAddedEvent event);
    }
}
