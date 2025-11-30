package dora.messenger.client.api;

import dora.messenger.protocol.chat.ChatDto;
import dora.messenger.protocol.contact.ContactDto;
import dora.messenger.protocol.contact.ContactRequestDto;
import dora.messenger.protocol.user.CreateUser;
import dora.messenger.protocol.user.CreatedUser;
import dora.messenger.protocol.user.UserDto;
import feign.Headers;
import feign.RequestLine;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface UserApi {

    @RequestLine("POST /users")
    @Headers("Content-Type: application/json")
    CompletableFuture<@NotNull CreatedUser> createUser(@NotNull CreateUser createUser);

    @RequestLine("GET /users/@self")
    CompletableFuture<@NotNull UserDto> getUser();

    @RequestLine("GET /users/@self/contacts")
    CompletableFuture<@NotNull Collection<ContactDto>> getContacts();

    @RequestLine("GET /users/@self/contact-requests")
    CompletableFuture<@NotNull Collection<ContactRequestDto>> getContactRequests();

    @RequestLine("GET /users/@self/chats")
    CompletableFuture<@NotNull Collection<ChatDto>> getChats();
}
