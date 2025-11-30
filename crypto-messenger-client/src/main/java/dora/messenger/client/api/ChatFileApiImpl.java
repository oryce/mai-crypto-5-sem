package dora.messenger.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dora.messenger.protocol.chat.BlobDto;
import dora.messenger.protocol.chat.file.ChatFileDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;

@Singleton
public class ChatFileApiImpl implements ChatFileApi {

    private final HttpClient httpClient;
    private final AuthenticationProvider authenticationProvider;
    private final ObjectMapper objectMapper;
    private final URI baseUri;

    @Inject
    public ChatFileApiImpl(
        @NotNull HttpClient httpClient,
        @NotNull AuthenticationProvider authenticationProvider,
        @NotNull ObjectMapper objectMapper
    ) {
        this.httpClient = requireNonNull(httpClient, "HTTP client");
        this.authenticationProvider = requireNonNull(authenticationProvider, "authentication provider");
        this.objectMapper = requireNonNull(objectMapper, "object mapper");

        // TODO (19.12.25, ~oryce):
        //   Don't hardcode base URI.
        this.baseUri = URI.create("http://localhost:8080");
    }

    @Override
    public CompletableFuture<@NotNull ChatFileDto> uploadFile(
        @NotNull UUID sessionId,
        @NotNull StreamBlobDto file,
        @NotNull BlobDto filename,
        @NotNull ChatFileApi.UploadCallback callback
    ) {
        requireNonNull(sessionId, "session ID");
        requireNonNull(file, "file");
        requireNonNull(filename, "filename");
        requireNonNull(callback, "progress callback");

        MultipartBodyPublisher multipartPublisher = new MultipartBodyPublisher()
            .addFile(
                "file",
                BodyPublishers.ofInputStream(file::stream),
                "file.bin", // File name doesn't matter
                file.length(),
                "application/octet-stream"
            )
            .addString("fileIv", file.iv(), "text/plain")
            .addString("filename", filename.ciphertext(), "text/plain")
            .addString("filenameIv", filename.iv(), "text/plain");

        ProgressBodyPublisher progressPublisher = new ProgressBodyPublisher(
            multipartPublisher.build(),
            multipartPublisher.length(),
            callback
        );

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .PUT(progressPublisher)
            .uri(baseUri.resolve("/chats/sessions/%s/files".formatted(sessionId)))
            .header(
                "Content-Type",
                "multipart/form-data; boundary=%s".formatted(multipartPublisher.boundary())
            );

        authenticationProvider.authentication().ifPresent(
            (authentication) -> authentication.applyToJava(builder)
        );

        return httpClient.sendAsync(builder.build(), BodyHandlers.ofByteArray())
            .thenApply((response) -> {
                try {
                    return objectMapper.readValue(response.body(), ChatFileDto.class);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read response body", e);
                }
            });
    }

    @Override
    public CompletableFuture<@NotNull FileDownload> downloadFile(@NotNull UUID sessionId, @NotNull UUID fileId) {
        requireNonNull(sessionId, "session ID");
        requireNonNull(fileId, "file ID");

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .GET()
            .uri(baseUri.resolve("/chats/sessions/%s/files/%s".formatted(sessionId, fileId)));

        authenticationProvider.authentication().ifPresent(
            (authentication) -> authentication.applyToJava(builder)
        );

        return httpClient.sendAsync(builder.build(), BodyHandlers.ofInputStream())
            .thenApply((response) -> {
                int length = response.headers()
                    .firstValue("Content-Length")
                    .map(Integer::parseInt)
                    .orElse(-1);
                return new FileDownload(response.body(), length);
            });
    }

    @Override
    public CompletableFuture<Void> deleteFile(@NotNull UUID sessionId, @NotNull UUID fileId) {
        requireNonNull(sessionId, "session ID");
        requireNonNull(fileId, "file ID");

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .DELETE()
            .uri(baseUri.resolve("/chats/sessions/%s/files/%s".formatted(sessionId, fileId)));

        authenticationProvider.authentication().ifPresent(
            (authentication) -> authentication.applyToJava(builder)
        );

        return httpClient.sendAsync(builder.build(), BodyHandlers.discarding())
            .thenApply(HttpResponse::body);
    }

    private static class MultipartBodyPublisher {

        private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.UTF_8);

        private final String boundary;
        private final Collection<BodyPublisher> bodyPublishers;

        private long length;

        public MultipartBodyPublisher() {
            this.boundary = "---MultipartBodyPublisher" + UUID.randomUUID();
            this.bodyPublishers = new ArrayList<>();
        }

        private String escape(String name) {
            return name.replace("\"", "\\\"");
        }

        public MultipartBodyPublisher addBytes(String name, byte[] value, String contentType) {
            byte[] header = """
                            --%s\r
                            Content-Disposition: form-data; name="%s"\r
                            Content-Type: %s\r
                            \r
                            """
                .formatted(boundary, escape(name), contentType)
                .getBytes(StandardCharsets.UTF_8);

            bodyPublishers.add(BodyPublishers.ofByteArray(header));
            length += header.length;

            bodyPublishers.add(BodyPublishers.ofByteArray(value));
            length += value.length;

            bodyPublishers.add(BodyPublishers.ofByteArray(CRLF));
            length += CRLF.length;

            return this;
        }

        public MultipartBodyPublisher addString(String name, String value, String mimeType) {
            String contentType = mimeType + "; charset=UTF-8";
            return addBytes(name, value.getBytes(StandardCharsets.UTF_8), contentType);
        }

        public MultipartBodyPublisher addFile(
            String name,
            BodyPublisher filePublisher,
            String fileName,
            long fileSize,
            String mimeType
        ) {
            if (fileSize < 0) {
                throw new IllegalArgumentException("File size cannot be negative");
            }

            byte[] header = """
                            --%s\r
                            Content-Disposition: form-data; name="%s"; filename="%s"\r
                            Content-Type: %s\r
                            \r
                            """
                .formatted(boundary, escape(name), escape(fileName), mimeType)
                .getBytes(StandardCharsets.UTF_8);

            bodyPublishers.add(BodyPublishers.ofByteArray(header));
            length += header.length;

            bodyPublishers.add(filePublisher);
            length += fileSize;

            bodyPublishers.add(BodyPublishers.ofByteArray(CRLF));
            length += CRLF.length;

            return this;
        }

        public BodyPublisher build() {
            byte[] footer = "--%s--\r\n"
                .formatted(boundary)
                .getBytes(StandardCharsets.UTF_8);

            bodyPublishers.add(BodyPublishers.ofByteArray(footer));
            length += footer.length;

            return BodyPublishers.concat(bodyPublishers.toArray(BodyPublisher[]::new));
        }

        public String boundary() {
            return boundary;
        }

        public long length() {
            return length;
        }
    }

    private static class ProgressBodyPublisher implements BodyPublisher {

        private final BodyPublisher delegate;
        private final long contentLength;
        private final UploadCallback callback;

        public ProgressBodyPublisher(
            BodyPublisher delegate,
            long contentLength,
            UploadCallback callback
        ) {
            this.delegate = delegate;
            this.contentLength = contentLength;
            this.callback = callback;
        }

        @Override
        public long contentLength() {
            return contentLength >= 0 ? contentLength : delegate.contentLength();
        }

        @Override
        public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
            delegate.subscribe(new Flow.Subscriber<>() {

                long transferred;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    transferred = 0;
                    onProgress(transferred);

                    subscriber.onSubscribe(subscription);
                }

                @Override
                public void onNext(ByteBuffer item) {
                    transferred += item.remaining();
                    onProgress(transferred);

                    subscriber.onNext(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    subscriber.onError(throwable);
                }

                @Override
                public void onComplete() {
                    onProgress(transferred);
                    subscriber.onComplete();
                }

                private void onProgress(long transferred) {
                    long contentedLength = contentLength();
                    callback.onProgress(transferred, contentedLength);
                }
            });
        }
    }
}
