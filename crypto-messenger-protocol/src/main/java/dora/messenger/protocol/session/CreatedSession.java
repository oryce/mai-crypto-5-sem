package dora.messenger.protocol.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreatedSession(
    @Schema(description = "Created session")
    @NotNull
    SessionDto session,

    @Schema(description = "Credentials for the created session")
    @NotNull
    SessionCredentialsDto credentials
) {
}
