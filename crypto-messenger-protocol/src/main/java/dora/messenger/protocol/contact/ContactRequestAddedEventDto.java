package dora.messenger.protocol.contact;

import dora.messenger.protocol.EventDto;

public class ContactRequestAddedEventDto extends EventDto {

    /** Added contact request. */
    private ContactRequestDto request;

    //region Accessors
    public ContactRequestDto getRequest() {
        return request;
    }

    public void setRequest(ContactRequestDto request) {
        this.request = request;
    }
    //endregion
}
