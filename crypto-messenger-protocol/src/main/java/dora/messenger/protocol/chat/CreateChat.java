package dora.messenger.protocol.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateChat(
    @Schema(description = "Participant ID")
    @NotNull
    UUID participantId,

    @Schema(description = "Diffie-Hellman group")
    @NotNull
    ChatDto.DiffieHellmanGroup dhGroup,

    @Schema(description = "Encryption algorithm")
    @NotNull
    ChatDto.Algorithm algorithm,

    @Schema(description = "Encryption mode")
    @NotNull
    ChatDto.CipherMode encryptionMode,

    @Schema(description = "Padding mode")
    @NotNull
    ChatDto.Padding paddingMode
) {
}
