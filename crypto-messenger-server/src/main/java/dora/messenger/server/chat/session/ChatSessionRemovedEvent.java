package dora.messenger.server.chat.session;

import dora.messenger.protocol.chat.session.ChatSessionRemovedEventDto;
import dora.messenger.server.event.Event;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.UUID;

/**
 * @param sessionId session ID
 * @param chatId    chat ID
 */
public record ChatSessionRemovedEvent(UUID sessionId, UUID chatId) implements Event {

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING)
    public interface Mapper {

        @Mapping(target = "id", ignore = true)
        ChatSessionRemovedEventDto toDto(ChatSessionRemovedEvent event);
    }
}
