package dora.messenger.server.chat.file;

import dora.messenger.server.chat.Blob;
import dora.messenger.server.chat.session.ChatSession;
import dora.messenger.server.chat.session.ChatSessionService;
import dora.messenger.server.chat.session.ChatSessionService.UserNotInvolvedException;
import dora.messenger.server.user.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@Validated
public class ChatFileService {

    private final ChatFileRepository files;
    private final ChatSessionService sessions;
    private final ChatFileStorage storage;

    public ChatFileService(
        ChatFileRepository files,
        ChatSessionService sessions,
        ChatFileStorage storage
    ) {
        this.files = files;
        this.sessions = sessions;
        this.storage = storage;
    }

    public record StreamBlob(String iv, InputStream stream) {
    }

    @Transactional
    public ChatFile putFile(
        @NotNull UUID sessionId,
        @NotNull User uploader,
        @NotNull StreamBlob file,
        @NotNull Blob filename
    ) throws IOException {
        ChatSession session = sessions.getSessionById(sessionId);

        if (!session.involvesUser(uploader))
            throw new UserNotInvolvedException(sessionId, uploader.getId());

        ChatFile chatFile = new ChatFile();
        chatFile.setSession(session);
        chatFile.setIv(file.iv());
        chatFile.setFilename(filename);
        files.save(chatFile);

        // Save the file to disk. If an `IOException` occurs, the database operation
        // is rolled back.
        storage.put(chatFile, file.stream());

        return chatFile;
    }

    public Resource getFile(UUID sessionId, UUID fileId, User user) {
        ChatSession session = sessions.getSessionById(sessionId);

        if (!session.involvesUser(user))
            throw new UserNotInvolvedException(sessionId, user.getId());

        ChatFile file = getFile(sessionId, fileId);
        return storage.get(file);
    }

    public List<ChatFile> resolveAll(Collection<UUID> ids, UUID sessionId) {
        return files.findByIdInAndSessionId(ids, sessionId);
    }

    @Transactional
    public void deleteFile(UUID sessionId, UUID fileId, User user) throws IOException {
        ChatSession session = sessions.getSessionById(sessionId);

        if (!session.involvesUser(user))
            throw new UserNotInvolvedException(sessionId, user.getId());

        ChatFile file = getFile(sessionId, fileId);
        files.delete(file);

        // Delete the file on disk. If an `IOException` occurs, the database operation
        // is rolled back.
        storage.delete(file);
    }

    private ChatFile getFile(UUID sessionId, UUID fileId) {
        return files.findByIdAndSessionId(fileId, sessionId)
            .orElseThrow(() -> new FileNotFoundException(sessionId, fileId));
    }

    public static final class FileNotFoundException extends RuntimeException {

        public FileNotFoundException(UUID sessionId, UUID fileId) {
            super("File \"%s\" does not exist in session \"%s\"".formatted(fileId, sessionId));
        }
    }
}
