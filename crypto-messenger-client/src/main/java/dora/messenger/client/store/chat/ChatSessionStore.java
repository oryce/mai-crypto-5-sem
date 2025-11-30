package dora.messenger.client.store.chat;

import dora.crypto.dh.DiffieHellman;
import dora.messenger.client.api.ChatSessionApi;
import dora.messenger.client.api.EventApi;
import dora.messenger.client.persistence.ChatSession;
import dora.messenger.client.persistence.ChatSessionRepository;
import dora.messenger.client.store.Computed;
import dora.messenger.client.store.chat.Chat.DiffieHellmanGroupId;
import dora.messenger.protocol.chat.session.ChatSessionInitiationEventDto;
import dora.messenger.protocol.chat.session.ChatSessionRemovedEventDto;
import dora.messenger.protocol.chat.session.ChatSessionResponseEventDto;
import dora.messenger.protocol.chat.session.CreateChatSession;
import dora.messenger.protocol.chat.session.EstablishChatSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

@Singleton
public class ChatSessionStore {

    private static final Logger LOGGER = LogManager.getLogger(ChatSessionStore.class);

    private final ChatSessionApi sessionApi;
    private final ChatSessionRepository sessions;

    @Inject
    public ChatSessionStore(
        @NotNull ChatSessionApi sessionApi,
        @NotNull ChatSessionRepository sessions,
        @NotNull EventApi eventApi
    ) {
        this.sessionApi = requireNonNull(sessionApi, "session API");
        this.sessions = requireNonNull(sessions, "session repository");

        requireNonNull(eventApi, "event API");

        eventApi.subscribe(ChatSessionInitiationEventDto.class, this::handleInitiation);
        eventApi.subscribe(ChatSessionRemovedEventDto.class, this::handleRemoved);
        eventApi.subscribe(ChatSessionResponseEventDto.class, this::handleResponse);
    }

    //region State
    private final Map<UUID, Computed<ChatSession>> computedSessions = new ConcurrentHashMap<>();

    private Computed<ChatSession> getSessionByChatId(UUID chatId) {
        // TODO (18.12.25, ~oryce):
        //   What happens under contention?
        return computedSessions.computeIfAbsent(chatId, this::createComputedSession);
    }

    private Computed<ChatSession> createComputedSession(UUID chatId) {
        AtomicReference<ChatSession> value = new AtomicReference<>();
        value.set(sessions.findByChatId(chatId).orElse(null));

        return Computed.of(
            value::get,
            (newSession) -> {
                if (newSession != null) {
                    value.set(newSession);
                    sessions.save(newSession);
                }
                else {
                    value.set(null);
                    sessions.deleteByChatId(chatId);
                }
            }
        );
    }

    public Computed<ChatSession> getSession(Chat chat) {
        return getSessionByChatId(chat.id());
    }
    //endregion

    //region Event handlers
    // FIXME (18.12.25, ~oryce):
    //   Key exchange takes a long time and stalls the WebSocket reader.

    private void handleInitiation(ChatSessionInitiationEventDto event) {
        // Initialize key exchange.
        DiffieHellmanGroupId groupId = Chat.MAPPER.dhGroupToDomain(event.getDhGroup());
        DiffieHellman keyExchange = DiffieHellman.of(groupId.group());

        // Generate private key and derive public key.
        BigInteger publicKey = keyExchange.initiate();
        BigInteger privateKey = keyExchange.privateKey();

        // Complete key exchange with initiator's public key.
        keyExchange.complete(decodePublicKey(event.getPublicKey()));
        BigInteger sharedSecret = keyExchange.sharedSecret();

        LOGGER.debug(
            "Completed key exchange {} (responder side) with shared secret {}",
            event.getSessionId(),
            hideSharedSecret(sharedSecret)
        );

        // Create and store chat session.
        ChatSession newSession = new ChatSession();
        newSession.setSessionId(event.getSessionId());
        newSession.setChatId(event.getChatId());
        newSession.setDhGroupId(groupId);
        newSession.setPrivateKey(privateKey);
        newSession.setSharedSecret(sharedSecret);

        Computed<ChatSession> storedSession = getSessionByChatId(event.getChatId());
        storedSession.set(newSession);

        // Send public key to initiator.
        sessionApi.establishSession(
            event.getSessionId(),
            new EstablishChatSession(encodePublicKey(publicKey))
        );
    }

    private void handleResponse(ChatSessionResponseEventDto event) {
        Computed<ChatSession> storedSession = getSessionByChatId(event.getChatId());
        ChatSession session = storedSession.get();

        if (session == null) {
            LOGGER.warn("Received key exchange response for unknown chat ({})", event.getChatId());
            return;
        }
        if (!event.getSessionId().equals(session.getSessionId())) {
            LOGGER.warn("Received key exchange response for different session ({})", event.getSessionId());
            return;
        }

        // Initialize key exchange.
        DiffieHellmanGroupId group = session.getDhGroupId();
        DiffieHellman keyExchange = DiffieHellman.of(group.group());

        // Resume with stored private key.
        BigInteger privateKey = new BigInteger(session.getPrivateKey());
        keyExchange.resume(privateKey);

        // Complete with responder's public key.
        keyExchange.complete(decodePublicKey(event.getPublicKey()));
        BigInteger sharedSecret = keyExchange.sharedSecret();

        LOGGER.debug(
            "Completed key exchange {} (initiator side) with shared secret {}",
            event.getSessionId(),
            hideSharedSecret(sharedSecret)
        );

        // Store shared secret.
        session.setSharedSecret(sharedSecret);
        storedSession.set(session);
    }

    private void handleRemoved(ChatSessionRemovedEventDto event) {
        Computed<ChatSession> storedSession = getSessionByChatId(event.getChatId());
        storedSession.set(null);
    }
    //endregion

    //region Actions
    public CompletableFuture<Void> createSession(Chat chat) {
        return CompletableFuture.supplyAsync(() -> initiateKeyExchange(chat))
            .thenCompose((initiateStage) -> sendPublicKey(chat, initiateStage))
            .thenAccept((storeStage) -> storeKeyExchangeState(chat, storeStage));
    }

    private InitiateStage initiateKeyExchange(Chat chat) {
        // Initialize key exchange.
        DiffieHellmanGroupId groupId = chat.dhGroup();
        DiffieHellman keyExchange = DiffieHellman.of(groupId.group());

        // Generate private key and derive public key.
        BigInteger publicKey = keyExchange.initiate();

        return new InitiateStage(keyExchange, publicKey);
    }

    private record InitiateStage(DiffieHellman keyExchange, BigInteger publicKey) {
    }

    private CompletableFuture<StoreStage> sendPublicKey(Chat chat, InitiateStage stage) {
        // Send public key to responder.
        return sessionApi.createSession(
                chat.id(),
                new CreateChatSession(encodePublicKey(stage.publicKey()))
            )
            .thenApply((session) -> new StoreStage(
                session.sessionId(),
                stage.keyExchange(),
                stage.publicKey()
            ));
    }

    private void storeKeyExchangeState(Chat chat, StoreStage stage) {
        // Store in-progress key exchange.
        ChatSession newSession = new ChatSession();
        newSession.setSessionId(stage.sessionId());
        newSession.setChatId(chat.id());
        newSession.setDhGroupId(chat.dhGroup());
        newSession.setPrivateKey(stage.keyExchange().privateKey());

        Computed<ChatSession> storedSession = getSessionByChatId(chat.id());
        storedSession.set(newSession);
    }

    private record StoreStage(UUID sessionId, DiffieHellman keyExchange, BigInteger publicKey) {
    }

    public CompletableFuture<Void> deleteSession(ChatSession session) {
        return sessionApi.deleteSession(session.getSessionId())
            .thenRun(() -> {
                Computed<ChatSession> storedSession = getSessionByChatId(session.getChatId());
                storedSession.set(null);
            });
    }
    //endregion

    private String encodePublicKey(BigInteger publicKey) {
        byte[] bytes = publicKey.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private BigInteger decodePublicKey(String publicKey) {
        byte[] bytes = Base64.getDecoder().decode(publicKey);
        return new BigInteger(bytes);
    }

    private String hideSharedSecret(BigInteger sharedSecret) {
        String secretString = sharedSecret.toString();

        if (secretString.length() < 5) {
            return secretString;
        }

        return "%s...%s".formatted(
            secretString.substring(0, 5),
            secretString.substring(secretString.length() - 5)
        );
    }
}
