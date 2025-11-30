package dora.messenger.protocol.chat.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatSessionDto(
    @Schema(description = "Session ID")
    @NotNull
    UUID sessionId,

    @Schema(description = "Chat ID")
    @NotNull
    UUID chatId
) {
}
