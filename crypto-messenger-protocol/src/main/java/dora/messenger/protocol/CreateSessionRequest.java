package dora.messenger.protocol;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateSessionRequest(
    @Schema(description = "Username")
    @NotBlank
    String username,

    @Schema(description = "Password")
    @NotBlank
    String password
) {
}
