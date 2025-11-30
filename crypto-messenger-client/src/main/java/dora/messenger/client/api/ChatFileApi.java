package dora.messenger.client.api;

import dora.messenger.protocol.chat.BlobDto;
import dora.messenger.protocol.chat.file.ChatFileDto;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ChatFileApi {

    CompletableFuture<@NotNull ChatFileDto> uploadFile(
        @NotNull UUID sessionId,
        @NotNull StreamBlobDto file,
        @NotNull BlobDto filename,
        @NotNull UploadCallback callback
    );

    /**
     * @param stream content stream
     * @param length content length in bytes
     * @param iv     initialization vector (Base64-encoded)
     */
    record StreamBlobDto(InputStream stream, long length, String iv) {
    }

    @FunctionalInterface
    interface UploadCallback {

        void onProgress(long transferredSize, long totalSize);
    }

    CompletableFuture<@NotNull FileDownload> downloadFile(
        @NotNull UUID sessionId,
        @NotNull UUID fileId
    );

    /**
     * @param stream content stream
     * @param length content length in bytes
     */
    record FileDownload(InputStream stream, long length) {
    }

    CompletableFuture<Void> deleteFile(@NotNull UUID sessionId, @NotNull UUID fileId);
}
