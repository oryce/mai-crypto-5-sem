package dora.messenger.server.chat.session;

import dora.messenger.protocol.chat.session.ChatSessionResponseEventDto;
import dora.messenger.server.event.Event;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

/**
 * @param sessionId session ID
 * @param chatId    chat ID
 * @param publicKey Base64-encoded responder public key
 */
public record ChatSessionResponseEvent(UUID sessionId, UUID chatId, String publicKey) implements Event {

    @org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
    public interface Mapper {

        @Mapping(target = "id", ignore = true)
        ChatSessionResponseEventDto toDto(ChatSessionResponseEvent event);
    }
}
