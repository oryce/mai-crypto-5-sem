package dora.messenger.client.api;

import dora.messenger.protocol.session.CreateSession;
import dora.messenger.protocol.session.CreatedSession;
import feign.Headers;
import feign.RequestLine;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface SessionApi {

    @RequestLine("POST /sessions")
    @Headers("Content-Type: application/json")
    CompletableFuture<@NotNull CreatedSession> createSession(@NotNull CreateSession createSession);

    @RequestLine("DELETE /sessions/@current")
    CompletableFuture<Void> deleteSession();
}
