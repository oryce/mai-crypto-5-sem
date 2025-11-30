package dora.messenger.client.persistence;

import dora.messenger.protocol.chat.file.ChatFileDto;
import dora.messenger.protocol.chat.message.ChatMessageDto;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ChatMessage {

    /** Message ID */
    private UUID id;
    /** Chat ID. */
    private UUID chatId;
    /** Chat session ID. */
    private UUID sessionId;
    /** Sender ID. */
    private UUID senderId;
    /** Timestamp at which the message was sent. */
    private Instant timestamp;
    /** Decrypted content. */
    private String content;
    /** Attachment IDs. */
    private List<UUID> attachmentIds;

    //region Accessors
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<UUID> getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(List<UUID> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }
    //endregion

    //region Mapper
    public static final Mapper MAPPER = Mappers.getMapper(Mapper.class);

    @org.mapstruct.Mapper
    public interface Mapper {

        @Mapping(target = "content", source = "content")
        @Mapping(target = "attachmentIds", source = "message.attachments")
        ChatMessage map(ChatMessageDto message, String content);

        default List<UUID> mapAttachments(List<ChatFileDto> attachments) {
            if (attachments == null) return List.of();
            return attachments.stream().map(ChatFileDto::id).toList();
        }
    }
    //endregion
}
