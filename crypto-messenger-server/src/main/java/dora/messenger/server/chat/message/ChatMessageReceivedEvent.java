package dora.messenger.server.chat.message;

import dora.messenger.protocol.chat.message.ChatMessageReceivedEventDto;
import dora.messenger.server.event.Event;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

public record ChatMessageReceivedEvent(ChatMessage message) implements Event {

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING, uses = { ChatMessage.Mapper.class })
    public interface Mapper {

        @Mapping(target = "id", ignore = true)
        ChatMessageReceivedEventDto map(ChatMessageReceivedEvent event);
    }
}
