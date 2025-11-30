package dora.messenger.protocol.contact;

import dora.messenger.protocol.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ContactDto(
    @Schema(description = "Contact ID")
    @NotNull
    UUID id,

    @Schema(description = "Participating user")
    @NotNull
    UserDto user
) {
}
