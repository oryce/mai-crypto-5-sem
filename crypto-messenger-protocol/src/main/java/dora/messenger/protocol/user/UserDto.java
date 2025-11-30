package dora.messenger.protocol.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserDto(
    @Schema(description = "User ID")
    @NotNull
    UUID id,

    @Schema(description = "First name")
    @NotNull
    String firstName,

    @Schema(description = "Last name")
    @NotNull
    String lastName,

    @Schema(description = "Username")
    @NotNull
    String username
) {
}
