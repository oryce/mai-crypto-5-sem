package dora.messenger.protocol.chat;

import dora.messenger.protocol.EventDto;

import java.util.UUID;

public class ChatRemovedEventDto extends EventDto {

    /** Removed chat ID. */
    private UUID chatId;

    //region Accessors
    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }
    //endregion
}
