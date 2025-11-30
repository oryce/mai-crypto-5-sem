package dora.messenger.server.chat.message;

import dora.messenger.protocol.chat.message.ChatMessageDto;
import dora.messenger.server.chat.Blob;
import dora.messenger.server.chat.file.ChatFile;
import dora.messenger.server.user.User;
import org.mapstruct.MappingConstants.ComponentModel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @param id        message ID
 * @param chatId    chat ID
 * @param sessionId chat session ID
 * @param senderId  sender ID
 * @param content   encrypted content
 */
public record ChatMessage(
    UUID id,
    UUID chatId,
    UUID sessionId,
    UUID senderId,
    Instant timestamp,
    Blob content,
    List<ChatFile> attachments
) {

    @org.mapstruct.Mapper(
        componentModel = ComponentModel.SPRING,
        uses = { Blob.Mapper.class, ChatFile.Mapper.class, User.Mapper.class }
    )
    public interface Mapper {

        ChatMessageDto map(ChatMessage message);
    }
}
