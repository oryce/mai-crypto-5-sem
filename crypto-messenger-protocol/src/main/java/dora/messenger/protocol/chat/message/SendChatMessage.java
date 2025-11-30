package dora.messenger.protocol.chat.message;

import dora.messenger.protocol.chat.BlobDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SendChatMessage(
    @Schema(description = "Encrypted content")
    @NotNull
    BlobDto content,

    @Schema(description = "Attachments IDs")
    @NotNull
    List<UUID> attachments
) {
}
