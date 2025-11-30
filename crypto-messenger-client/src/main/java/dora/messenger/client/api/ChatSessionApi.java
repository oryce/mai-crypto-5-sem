package dora.messenger.client.api;

import dora.messenger.protocol.chat.session.ChatSessionDto;
import dora.messenger.protocol.chat.session.CreateChatSession;
import dora.messenger.protocol.chat.session.EstablishChatSession;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ChatSessionApi {

    @RequestLine("POST /chats/{chatId}/session")
    @Headers("Content-Type: application/json")
    CompletableFuture<@NotNull ChatSessionDto> createSession(
        @Param("chatId") UUID chatId,
        @NotNull CreateChatSession createSession
    );

    @RequestLine("PATCH /chats/sessions/{sessionId}")
    @Headers("Content-Type: application/json")
    CompletableFuture<Void> establishSession(
        @Param("sessionId") UUID sessionId,
        @NotNull EstablishChatSession establishSession
    );

    @RequestLine("DELETE /chats/sessions/{sessionId}")
    CompletableFuture<Void> deleteSession(@Param("sessionId") UUID sessionId);
}
