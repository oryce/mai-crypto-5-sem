package dora.messenger.client.api;

import dora.messenger.protocol.chat.message.ChatMessageDto;
import dora.messenger.protocol.chat.message.SendChatMessage;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ChatMessageApi {

    @RequestLine("POST /chats/sessions/{sessionId}/messages")
    @Headers("Content-Type: application/json")
    CompletableFuture<@NotNull ChatMessageDto> sendMessage(
        @Param("sessionId") @NotNull UUID sessionId,
        @NotNull SendChatMessage sendMessage
    );
}
