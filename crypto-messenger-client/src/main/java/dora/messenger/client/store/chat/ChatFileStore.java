package dora.messenger.client.store.chat;

import dora.crypto.SymmetricCipher;
import dora.messenger.client.api.ChatFileApi;
import dora.messenger.client.api.ChatFileApi.FileDownload;
import dora.messenger.client.api.ChatFileApi.StreamBlobDto;
import dora.messenger.client.persistence.ChatFile;
import dora.messenger.client.persistence.ChatFileRepository;
import dora.messenger.client.persistence.ChatSession;
import dora.messenger.client.store.Computed;
import dora.messenger.protocol.chat.BlobDto;
import dora.messenger.protocol.chat.file.ChatFileDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

@Singleton
public class ChatFileStore {

    private final ChatFileApi chatFileApi;
    private final ChatFileRepository chatFiles;
    private final SecureRandom random;
    private final Path storageDirectory;

    @Inject
    public ChatFileStore(
        @NotNull ChatFileApi chatFileApi,
        @NotNull ChatFileRepository chatFiles,
        @NotNull SecureRandom random
    ) {
        this.chatFileApi = requireNonNull(chatFileApi, "chat file API");
        this.chatFiles = requireNonNull(chatFiles, "chat file repository");
        this.random = requireNonNull(random, "random");

        // TODO (20.12.25, ~oryce):
        //   Don't hardcode storage path.
        try {
            storageDirectory = Paths.get("storage");
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create storage directory", e);
        }
    }

    //region State
    private final Map<UUID, Computed<ChatFile>> computedFiles = new ConcurrentHashMap<>();

    public Computed<ChatFile> getById(UUID fileId) {
        return computedFiles.computeIfAbsent(fileId, this::createComputedFile);
    }

    private Computed<ChatFile> createComputedFile(UUID fileId) {
        AtomicReference<ChatFile> value = new AtomicReference<>();
        value.set(chatFiles.findById(fileId).orElse(null));

        return Computed.of(
            value::get,
            (newFile) -> {
                requireNonNull(newFile, "new file cannot be null");
                value.set(newFile);
                chatFiles.save(newFile);
            }
        );
    }
    //endregion

    //region Actions
    //region uploadFile
    public CompletableFuture<ChatFile> uploadFile(
        @NotNull Chat chat,
        @NotNull ChatSession session,
        @NotNull Path filePath,
        @NotNull ProgressCallback callback
    ) {
        requireNonNull(chat, "chat");
        requireNonNull(session, "chat session");
        requireNonNull(filePath, "file path");
        requireNonNull(callback, "callback");

        // FIXME (20.12.25, ~oryce):
        //   Cancelling the returned future doesn't prevent the file
        //   from being uploaded.

        return CompletableFuture.supplyAsync(() -> {
                try {
                    return encryptFile(chat, session, filePath, callback);
                } catch (IOException e) {
                    throw new UncheckedIOException("Cannot encrypt file", e);
                }
            })
            .thenApply((fileBlob) -> {
                try {
                    BlobDto filenameBlob = encryptFilename(chat, session, filePath);
                    return new FileAndFilenameBlobs(fileBlob, filenameBlob);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Cannot encrypt filename", e);
                }
            })
            .thenCompose((blobs) -> chatFileApi.uploadFile(
                session.getSessionId(),
                blobs.fileBlob(),
                blobs.filenameBlob(),
                callback::onProgress
            ))
            .thenApply((file) -> storeRecord(session, filePath, file));
    }

    private StreamBlobDto encryptFile(
        Chat chat,
        ChatSession session,
        Path filePath,
        ProgressCallback callback
    ) throws IOException {
        byte[] iv = EncryptionSupport.generateIv(chat, random);

        SymmetricCipher cipher = EncryptionSupport.createCipher(chat, session, iv);
        InputStream cipherStream = cipher.encryptingInputStream(Files.newInputStream(filePath));

        long length = paddedLength(chat.algorithm(), Files.size(filePath));
        InputStream progressStream = new ProgressInputStream(cipherStream, callback, length);

        return new StreamBlobDto(progressStream, length, Base64.getEncoder().encodeToString(iv));
    }

    private long paddedLength(Chat.Algorithm algorithm, long length) {
        int blockSize = algorithm.createCipher().blockSize();
        return length + (blockSize - (length % blockSize));
    }

    private BlobDto encryptFilename(Chat chat, ChatSession session, Path filePath)
    throws InterruptedException {
        String filename = filePath.getFileName().toString();
        byte[] iv = EncryptionSupport.generateIv(chat, random);
        return EncryptionSupport.encryptString(chat, session, filename, iv);
    }

    private record FileAndFilenameBlobs(StreamBlobDto fileBlob, BlobDto filenameBlob) {
    }

    private ChatFile storeRecord(ChatSession session, Path filePath, ChatFileDto uploadedFile) {
        ChatFile file = new ChatFile();

        file.setId(uploadedFile.id());
        file.setSessionId(session.getSessionId());
        file.setIv(Base64.getDecoder().decode(uploadedFile.iv()));
        file.setFilename(filePath.getFileName().toString());
        file.setLocation(filePath.toAbsolutePath().toString());

        chatFiles.save(file);
        return file;
    }
    //endregion

    //region downloadFile
    public CompletableFuture<Void> downloadFile(
        @NotNull Chat chat,
        @NotNull ChatSession session,
        @NotNull Computed<ChatFile> computedFile,
        @NotNull ProgressCallback callback
    ) {
        requireNonNull(chat, "chat");
        requireNonNull(session, "chat session");
        requireNonNull(computedFile, "computed file");
        requireNonNull(callback, "callback");

        ChatFile file = computedFile.get();

        if (file == null)
            throw new IllegalArgumentException("Unknown file");
        if (!file.getSessionId().equals(session.getSessionId()))
            throw new IllegalArgumentException("File cannot be decrypted by session");

        return chatFileApi.downloadFile(session.getSessionId(), file.getId())
            .thenApply((download) -> {
                try {
                    return downloadFile(download, chat, session, file, callback);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .thenCompose((filePath) ->
                chatFileApi.deleteFile(session.getSessionId(), file.getId())
                    .thenApply((nothing) -> filePath)
            )
            .thenAccept((filePath) -> {
                file.setLocation(filePath.toAbsolutePath().toString());
                computedFile.set(file);
            });
    }

    private Path downloadFile(
        FileDownload download,
        Chat chat,
        ChatSession session,
        ChatFile file,
        ProgressCallback callback
    )
    throws IOException {
        Path filePath = storageDirectory.resolve(file.getFilename());

        try (OutputStream storeStream = Files.newOutputStream(filePath)) {
            SymmetricCipher cipher = EncryptionSupport.createCipher(chat, session, file.getIv());
            InputStream decryptStream = cipher.decryptingInputStream(download.stream());
            InputStream progressStream = new ProgressInputStream(decryptStream, callback, download.length());
            progressStream.transferTo(storeStream);
        }

        return filePath;
    }
    //endregion

    public interface ProgressCallback {

        void onProgress(long transferredSize, long totalSize);
    }
    //endregion

    private static class ProgressInputStream extends FilterInputStream {

        private long transferredSize;

        private final ProgressCallback callback;
        private final long totalSize;

        public ProgressInputStream(InputStream stream, ProgressCallback callback, long totalSize) {
            super(stream);

            this.callback = callback;
            this.totalSize = totalSize;
        }

        @Override
        public int read() throws IOException {
            int read = super.read();

            if (read != -1) {
                transferredSize++;
                onProgress();
            }

            return read;
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            int read = super.read(b, off, len);

            if (read > 0) {
                transferredSize += read;
                onProgress();
            }

            return read;
        }

        private void onProgress() {
            callback.onProgress(transferredSize, totalSize);
        }
    }
}
