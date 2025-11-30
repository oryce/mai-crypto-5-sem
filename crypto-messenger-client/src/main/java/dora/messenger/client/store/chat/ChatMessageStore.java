package dora.messenger.client.store.chat;

import dora.messenger.client.api.ChatMessageApi;
import dora.messenger.client.api.EventApi;
import dora.messenger.client.persistence.ChatFile;
import dora.messenger.client.persistence.ChatMessage;
import dora.messenger.client.persistence.ChatMessageRepository;
import dora.messenger.client.persistence.ChatSession;
import dora.messenger.client.persistence.ChatSessionRepository;
import dora.messenger.client.store.ObservableCollection;
import dora.messenger.protocol.chat.file.ChatFileDto;
import dora.messenger.protocol.chat.message.ChatMessageDto;
import dora.messenger.protocol.chat.message.ChatMessageReceivedEventDto;
import dora.messenger.protocol.chat.message.SendChatMessage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static dora.messenger.client.store.chat.EncryptionSupport.decryptString;
import static dora.messenger.client.store.chat.EncryptionSupport.encryptString;
import static dora.messenger.client.store.chat.EncryptionSupport.generateIv;
import static java.util.Objects.requireNonNull;

@Singleton
public class ChatMessageStore {

    private static final Logger LOGGER = LogManager.getLogger(ChatMessageStore.class);

    private final ChatMessageApi messageApi;
    private final ChatMessageRepository messages;
    private final ChatSessionRepository sessions;
    private final ChatStore chats;
    private final ChatFileStore files;
    private final SecureRandom random;

    @Inject
    public ChatMessageStore(
        @NotNull ChatMessageApi messageApi,
        @NotNull ChatMessageRepository messages,
        @NotNull ChatSessionRepository sessions,
        @NotNull ChatStore chats,
        @NotNull ChatFileStore files,
        @NotNull EventApi eventApi
    ) {
        this.messageApi = requireNonNull(messageApi, "message API");
        this.messages = requireNonNull(messages, "message repository");
        this.sessions = requireNonNull(sessions, "session repository");
        this.chats = requireNonNull(chats, "chat store");
        this.files = requireNonNull(files, "file store");
        this.random = new SecureRandom();

        requireNonNull(eventApi, "event API");

        eventApi.subscribe(ChatMessageReceivedEventDto.class, this::handleMessageReceived);
    }

    //region State
    private final Map<UUID, MessageCollectionRef> messageRefs = new ConcurrentHashMap<>();

    private MessageCollectionRef getMessagesByChatId(UUID chatId) {
        return messageRefs.computeIfAbsent(chatId, MessageCollectionRef::new);
    }

    public MessageCollectionRef getMessages(Chat chat) {
        return getMessagesByChatId(chat.id());
    }

    public class MessageCollectionRef extends ObservableCollection<ChatMessage> {

        private final Collection<ChatMessage> messageCache;

        public MessageCollectionRef(@NotNull UUID chatId) {
            requireNonNull(chatId, "chat ID");
            this.messageCache = new ArrayList<>(messages.findAllByChatId(chatId));
        }

        public Collection<ChatMessage> get() {
            return Collections.unmodifiableCollection(messageCache);
        }

        public void add(ChatMessage message) {
            messages.save(message);

            messageCache.add(message);
            notifyAdded(message);
        }
    }
    //endregion

    //region Event handlers
    public void handleMessageReceived(ChatMessageReceivedEventDto event) {
        ChatMessageDto message = event.getMessage();

        ChatSession session = sessions.findByChatId(message.chatId())
            .orElse(null);
        if (session == null || !message.sessionId().equals(session.getSessionId())) {
            LOGGER.warn(
                "Received message {} for unknown or different session {}",
                message.id(),
                message.chatId()
            );
            return;
        }

        Chat chat = chats.findChatById(message.chatId())
            .orElse(null);
        if (chat == null || !message.chatId().equals(chat.id())) {
            LOGGER.warn(
                "Received message {} for unknown or different chat {}",
                message.id(),
                message.chatId()
            );
            return;
        }

        // Decrypt message.
        String content;
        try {
            content = decryptString(chat, session, message.content());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (Exception e) {
            LOGGER.error("Unable to decrypt message {}", message.id(), e);
            return;
        }

        // Map attachments (decrypt filenames.)
        List<ChatFile> attachments = new ArrayList<>();
        for (ChatFileDto attachment : message.attachments()) {
            try {
                attachments.add(mapAttachment(chat, session, attachment));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                LOGGER.error("Unable to map attachment {}", attachment.id(), e);
                return;
            }
        }

        storeMessage(chat, message, content, attachments);
    }

    private static ChatFile mapAttachment(Chat chat, ChatSession session, ChatFileDto fileDto)
    throws InterruptedException {
        ChatFile file = new ChatFile();

        file.setId(fileDto.id());
        file.setSessionId(session.getSessionId());
        file.setIv(Base64.getDecoder().decode(fileDto.iv()));
        file.setFilename(decryptString(chat, session, fileDto.filename()));

        return file;
    }
    //endregion

    //region Actions
    public CompletableFuture<Void> sendMessage(
        @NotNull Chat chat,
        @NotNull ChatSession session,
        @NotNull String content,
        @NotNull List<ChatFile> attachments
    ) {
        return CompletableFuture.supplyAsync(() -> {
                try {
                    byte[] iv = generateIv(chat, random);
                    return encryptString(chat, session, content, iv);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            })
            .thenCompose((encryptedContent) -> {
                List<UUID> attachmentIds = attachments.stream().map(ChatFile::getId).toList();

                return messageApi.sendMessage(
                    session.getSessionId(),
                    new SendChatMessage(encryptedContent, attachmentIds)
                );
            })
            .thenAccept((message) -> storeMessage(chat, message, content, attachments));
    }
    //endregion

    private void storeMessage(
        Chat chat,
        ChatMessageDto message,
        String content,
        List<ChatFile> attachments
    ) {
        // Persist attachments.
        attachments.forEach((attachment) ->
            files.getById(attachment.getId()).set(attachment)
        );

        // Persist message.
        ChatMessage plainMessage = ChatMessage.MAPPER.map(message, content);
        MessageCollectionRef messages = getMessagesByChatId(chat.id());
        messages.add(plainMessage);
    }
}
