package dora.messenger.client.persistence;

import dora.messenger.client.store.chat.Chat.DiffieHellmanGroupId;

import java.math.BigInteger;
import java.util.UUID;

public class ChatSession {

    /** Session ID. */
    private UUID sessionId;
    /** Chat ID. */
    private UUID chatId;
    /** Diffie-Hellman group ID. */
    private DiffieHellmanGroupId dhGroupId;
    /** Diffie-Hellman secret. */
    private byte[] privateKey;
    /** Diffie-Hellman shared secret. */
    private byte[] sharedSecret;

    public boolean isComplete() {
        return sharedSecret != null;
    }

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

    public DiffieHellmanGroupId getDhGroupId() {
        return dhGroupId;
    }

    public void setDhGroupId(DiffieHellmanGroupId dhGroupId) {
        this.dhGroupId = dhGroupId;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public void setPrivateKey(BigInteger publicKey) {
        setPrivateKey(publicKey.toByteArray());
    }

    public byte[] getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(byte[] sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public void setSharedSecret(BigInteger sharedSecret) {
        setSharedSecret(sharedSecret.toByteArray());
    }
    //endregion
}
