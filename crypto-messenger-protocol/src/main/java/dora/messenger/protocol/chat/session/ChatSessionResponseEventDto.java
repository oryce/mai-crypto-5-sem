package dora.messenger.protocol.chat.session;

import dora.messenger.protocol.EventDto;

import java.util.UUID;

public class ChatSessionResponseEventDto extends EventDto {

    /** Session ID. */
    private UUID sessionId;
    /** Chat ID. */
    private UUID chatId;
    /** Responder's public key (Base64-encoded). */
    private String publicKey;

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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    //endregion
}
