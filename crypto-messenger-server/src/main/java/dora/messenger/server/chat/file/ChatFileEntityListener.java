package dora.messenger.server.chat.file;

import jakarta.persistence.PostRemove;

import java.io.IOException;
import java.io.UncheckedIOException;

public class ChatFileEntityListener {

    private final ChatFileStorage storage;

    public ChatFileEntityListener(ChatFileStorage storage) {
        this.storage = storage;
    }

    @PostRemove
    public void postRemove(ChatFile file) {
        try {
            storage.delete(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot remove file from storage", e);
        }
    }
}
