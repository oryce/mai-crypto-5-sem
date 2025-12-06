package dora.messenger.protocol.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateContactRequest(
    @Schema(description = "Responding user's username")
    @NotNull
    String username
) {
}
