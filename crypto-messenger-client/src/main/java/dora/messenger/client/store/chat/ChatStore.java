package dora.messenger.client.store.chat;

import dora.crypto.SymmetricCipher;
import dora.messenger.client.api.ChatApi;
import dora.messenger.client.api.EventApi;
import dora.messenger.client.api.UserApi;
import dora.messenger.client.store.CollectionRef;
import dora.messenger.protocol.chat.ChatAddedEventDto;
import dora.messenger.protocol.chat.ChatRemovedEventDto;
import dora.messenger.protocol.chat.CreateChat;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

@Singleton
public class ChatStore {

    private final ChatApi chatApi;
    private final UserApi userApi;

    @Inject
    public ChatStore(@NotNull ChatApi chatApi, @NotNull UserApi userApi, @NotNull EventApi eventApi) {
        this.chatApi = requireNonNull(chatApi, "chat API");
        this.userApi = requireNonNull(userApi, "user API");

        eventApi.subscribe(ChatAddedEventDto.class, (event) -> {
            Collection<Chat> chats = chatsRef.get();
            if (chats != null) chatsRef.add(Chat.MAPPER.toDomain(event.getChat()));
        });

        eventApi.subscribe(ChatRemovedEventDto.class, (event) -> {
            Collection<Chat> chats = chatsRef.get();
            if (chats != null) chatsRef.removeIf((chat) -> chat.id().equals(event.getChatId()));
        });
    }

    private final CollectionRef<Chat> chatsRef = CollectionRef.ofNull();

    public CollectionRef<Chat> getChats() {
        return chatsRef;
    }

    public Optional<Chat> findChatById(UUID chatId) {
        Collection<Chat> chats = chatsRef.get();
        return chats.stream()
            .filter((chat) -> chat.id().equals(chatId))
            .findFirst();
    }

    public CompletableFuture<Void> fetchChats() {
        return userApi.getChats().thenAccept((chatDtos) -> {
            Collection<Chat> chats = new TreeSet<>(
                chatDtos.stream().map(Chat.MAPPER::toDomain).toList()
            );
            chatsRef.set(chats);
        });
    }

    public CompletableFuture<Void> createChat(
        @NotNull UUID participantId,
        @NotNull Chat.DiffieHellmanGroupId dhGroupId,
        @NotNull Chat.Algorithm algorithm,
        @NotNull SymmetricCipher.CipherModeType cipherMode,
        @NotNull SymmetricCipher.PaddingType padding
    ) {
        requireNonNull(participantId, "participant ID");
        requireNonNull(dhGroupId, "Diffie-Hellman group ID");
        requireNonNull(algorithm, "algorithm");
        requireNonNull(cipherMode, "cipher mode");
        requireNonNull(padding, "padding");

        return chatApi.create(new CreateChat(
            participantId,
            Chat.MAPPER.dhGroupToDto(dhGroupId),
            Chat.MAPPER.algorithmToDto(algorithm),
            Chat.MAPPER.cipherModeToDto(cipherMode),
            Chat.MAPPER.paddingToDto(padding)
        ))
            .thenAccept((chat) -> chatsRef.add(Chat.MAPPER.toDomain(chat)));
    }

    public CompletableFuture<Void> deleteChat(Chat chat) {
        return chatApi.delete(chat.id()).thenRun(() -> chatsRef.remove(chat));
    }
}
