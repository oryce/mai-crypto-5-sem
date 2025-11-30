package dora.messenger.protocol.chat.session;

import dora.messenger.protocol.EventDto;
import dora.messenger.protocol.chat.ChatDto.DiffieHellmanGroup;

import java.util.UUID;

public class ChatSessionInitiationEventDto extends EventDto {

    /** Session ID. */
    private UUID sessionId;
    /** Chat ID. */
    private UUID chatId;
    /** Chat Diffie-Hellman group. */
    private DiffieHellmanGroup dhGroup;
    /** Initiator's public key (Base64-encoded). */
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

    public DiffieHellmanGroup getDhGroup() {
        return dhGroup;
    }

    public void setDhGroup(DiffieHellmanGroup dhGroup) {
        this.dhGroup = dhGroup;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    //endregion
}
