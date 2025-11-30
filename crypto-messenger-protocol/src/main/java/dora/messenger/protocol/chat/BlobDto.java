package dora.messenger.protocol.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record BlobDto(
    @Schema(description = "Initialization vector (Base64-encoded)")
    @NotNull
    String iv,

    @Schema(description = "Ciphertext (Base64-encoded)")
    @NotNull
    String ciphertext
) {
}
