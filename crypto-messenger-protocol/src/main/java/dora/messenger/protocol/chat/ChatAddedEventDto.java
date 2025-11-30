package dora.messenger.protocol.chat;

import dora.messenger.protocol.EventDto;

public class ChatAddedEventDto extends EventDto {

    /** Added chat. */
    private ChatDto chat;

    //region Accessors
    public ChatDto getChat() {
        return chat;
    }

    public void setChat(ChatDto chat) {
        this.chat = chat;
    }
    //endregion
}
