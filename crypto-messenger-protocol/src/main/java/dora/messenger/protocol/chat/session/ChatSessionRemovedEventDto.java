package dora.messenger.protocol.chat.session;

import dora.messenger.protocol.EventDto;

import java.util.UUID;

public class ChatSessionRemovedEventDto extends EventDto {

    /** Session ID. */
    private UUID sessionId;
    /** Chat ID. */
    private UUID chatId;

    //region Accessors
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }
    //endregion
}
