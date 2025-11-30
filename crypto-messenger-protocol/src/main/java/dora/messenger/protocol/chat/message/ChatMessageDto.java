package dora.messenger.protocol.chat.message;

import dora.messenger.protocol.chat.BlobDto;
import dora.messenger.protocol.chat.file.ChatFileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatMessageDto(
    @Schema(description = "Message ID")
    @NotNull
    UUID id,

    @Schema(description = "Chat ID")
    @NotNull
    UUID chatId,

    @Schema(description = "Chat session ID")
    @NotNull
    UUID sessionId,

    @Schema(description = "Sender ID")
    @NotNull
    UUID senderId,

    @Schema(description = "Timestamp at which the message was sent")
    @NotNull
    Instant timestamp,

    @Schema(description = "Encrypted content")
    @NotNull
    BlobDto content,

    @Schema(description = "Attachments")
    @NotNull
    List<ChatFileDto> attachments
) {
}
