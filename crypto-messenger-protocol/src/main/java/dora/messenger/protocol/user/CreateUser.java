package dora.messenger.protocol.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUser(
    @Schema(description = "First name")
    @Size(min = 1, max = 32)
    @NotBlank
    String firstName,

    @Schema(description = "Last name")
    @Size(min = 1, max = 32)
    @NotBlank
    String lastName,

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
