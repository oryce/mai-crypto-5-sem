package dora.messenger.protocol.chat.file;

import dora.messenger.protocol.chat.BlobDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatFileDto(
    @Schema(description = "File ID")
    @NotNull
    UUID id,

    @Schema(description = "Initialization vector (Base64-encoded)")
    @NotNull
    String iv,

    @Schema(description = "Encrypted filename")
    @NotNull
    BlobDto filename
) {
}
