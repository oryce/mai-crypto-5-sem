package dora.messenger.protocol.contact;

import dora.messenger.protocol.EventDto;

public class ContactAddedEventDto extends EventDto {

    /** Added contact. */
    private ContactDto contact;

    //region Accessors
    public ContactDto getContact() {
        return contact;
    }

    public void setContact(ContactDto contact) {
        this.contact = contact;
    }
    //endregion
}
