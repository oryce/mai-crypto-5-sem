package dora.messenger.protocol.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record SessionCredentialsDto(
    @Schema(description = "Access token (used in the \"Authorization\" header)")
    @NotNull
    String accessToken
) {
}
