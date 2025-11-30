package dora.messenger.server.chat.session;

import dora.messenger.protocol.chat.ChatDto;
import dora.messenger.protocol.chat.session.ChatSessionInitiationEventDto;
import dora.messenger.server.chat.Chat.DiffieHellmanGroup;
import dora.messenger.server.event.Event;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.UUID;

/**
 * @param sessionId session ID
 * @param chatId    chat ID
 * @param dhGroup   chat Diffie-Hellman group
 * @param publicKey Base64-encoded initiator public key
 */
public record ChatSessionInitiationEvent(
    UUID sessionId,
    UUID chatId,
    DiffieHellmanGroup dhGroup,
    String publicKey
) implements Event {

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING)
    public interface Mapper {

        @Mapping(target = "id", ignore = true)
        ChatSessionInitiationEventDto toDto(ChatSessionInitiationEvent event);

        ChatDto.DiffieHellmanGroup dhGroupToDto(DiffieHellmanGroup dhGroup);
    }
}
