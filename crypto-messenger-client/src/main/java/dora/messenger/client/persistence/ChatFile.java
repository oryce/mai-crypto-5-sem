package dora.messenger.client.persistence;

import java.util.UUID;

public class ChatFile {

    /** File ID. */
    private UUID id;
    /** Session ID. */
    private UUID sessionId;
    /** Initialization vector. */
    private byte[] iv;
    /** Plaintext filename. */
    private String filename;
    /** Absolute path to file location. */
    private String location;

    //region Accessors
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    //endregion
}
