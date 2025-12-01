package dora.messenger.protocol;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
    @Schema(description = "Username. May not be reused across accounts")
    @Pattern(
        regexp = "\\w{3,16}",
        message = "Username must contain 3-16 alphanumeric characters"
    )
    String username,

    @Schema(description = "Password")
    @Pattern(
        regexp = "\\S{8,128}",
        message = "Password must contain 8-128 non-blank characters"
    )
    String password
) {
}
