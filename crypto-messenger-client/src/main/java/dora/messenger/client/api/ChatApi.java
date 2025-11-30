package dora.messenger.client.api;

import dora.messenger.protocol.chat.ChatDto;
import dora.messenger.protocol.chat.CreateChat;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ChatApi {

    @RequestLine("POST /chats")
    @Headers("Content-Type: application/json")
    CompletableFuture<@NotNull ChatDto> create(@NotNull CreateChat createChat);

    @RequestLine("DELETE /chats/{chatId}")
    CompletableFuture<Void> delete(@Param("chatId") @NotNull UUID chatId);
}
