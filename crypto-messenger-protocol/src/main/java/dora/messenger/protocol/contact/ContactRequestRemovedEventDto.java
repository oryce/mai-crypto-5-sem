package dora.messenger.protocol.contact;

import dora.messenger.protocol.EventDto;

import java.util.UUID;

public class ContactRequestRemovedEventDto extends EventDto {

    /** Removed contact request ID. */
    private UUID requestId;

    //region Accessors
    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }
    //endregion
}
