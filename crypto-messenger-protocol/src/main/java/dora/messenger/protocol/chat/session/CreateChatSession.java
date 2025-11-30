package dora.messenger.protocol.chat.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreateChatSession(
    @Schema(description = "Initiator's public key (Base64-encoded)")
    @NotNull
    String publicKey
) {
}
