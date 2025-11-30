package dora.messenger.protocol.contact;

import dora.messenger.protocol.EventDto;

import java.util.UUID;

public class ContactRemovedEventDto extends EventDto {

    /** Removed contact ID. */
    private UUID contactId;

    //region Accessors
    public UUID getContactId() {
        return contactId;
    }

    public void setContactId(UUID contactId) {
        this.contactId = contactId;
    }
    //endregion
}
