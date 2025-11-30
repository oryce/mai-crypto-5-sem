package dora.messenger.protocol.chat.message;

import dora.messenger.protocol.EventDto;

public class ChatMessageReceivedEventDto extends EventDto {

    /** Received message. */
    private ChatMessageDto message;

    //region Accessors
    public ChatMessageDto getMessage() {
        return message;
    }

    public void setMessage(ChatMessageDto message) {
        this.message = message;
    }
    //endregion
}
