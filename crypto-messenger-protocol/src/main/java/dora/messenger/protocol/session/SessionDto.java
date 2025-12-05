package dora.messenger.protocol.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SessionDto(
    @Schema(description = "Session ID")
    @NotNull
    UUID id
) {
}
