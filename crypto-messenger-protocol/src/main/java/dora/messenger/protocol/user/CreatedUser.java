package dora.messenger.protocol.user;

import dora.messenger.protocol.session.SessionCredentialsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreatedUser(
    @Schema(description = "Created user")
    @NotNull
    UserDto user,

    @Schema(description = "Session credentials for the created user")
    @NotNull
    SessionCredentialsDto credentials
) {
}
