package dora.messenger.protocol.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatDto(
    @Schema(description = "Chat ID")
    @NotNull
    UUID id,

    @Schema(description = "Chat name")
    @NotNull
    String name,

    @Schema(description = "Diffie-Hellman group")
    @NotNull
    DiffieHellmanGroup dhGroup,

    @Schema(description = "Encryption algorithm")
    @NotNull
    Algorithm algorithm,

    @Schema(description = "Cipher mode")
    @NotNull
    CipherMode cipherMode,

    @Schema(description = "Padding")
    @NotNull
    Padding padding
) {

    public enum DiffieHellmanGroup {

        FFDHE2048,
        FFDHE3072,
        FFDHE4096,
        FFDHE6144,
        FFDHE8192
    }

    public enum Algorithm {

        RC5,
        RC6
    }

    public enum CipherMode {

        CBC,
        CFB,
        CTR,
        ECB,
        OFB,
        PCBC,
        RANDOM_DELTA
    }

    public enum Padding {

        ANSI_X923,
        ISO_10126,
        PKCS7,
        ZEROS
    }
}
