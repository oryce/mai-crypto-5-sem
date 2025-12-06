package dora.messenger.protocol.contact;

import dora.messenger.protocol.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ContactRequestDto(
    @Schema(description = "Request ID")
    @NotNull
    UUID id,

    @Schema(description = "Request direction")
    @NotNull
    Direction direction,

    @Schema(description = "Initiating or responding user")
    @NotNull
    UserDto user
) {

    public enum Direction {

        INCOMING,
        OUTGOING
    }
}
