package dora.messenger.protocol.contact;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateContactRequest(
    @Schema(description = "Whether the request is approved or rejected")
    boolean approved
) {
}
