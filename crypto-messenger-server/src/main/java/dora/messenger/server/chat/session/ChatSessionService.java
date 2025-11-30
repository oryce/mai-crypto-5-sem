package dora.messenger.server.chat.session;

import dora.messenger.server.chat.Chat;
import dora.messenger.server.chat.ChatService;
import dora.messenger.server.event.EventService;
import dora.messenger.server.user.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@Validated
public class ChatSessionService {

    private final ChatSessionRepository sessions;
    private final ChatService chats;
    private final EventService events;

    public ChatSessionService(
        ChatSessionRepository sessions,
        ChatService chats,
        EventService events
    ) {
        this.sessions = sessions;
        this.chats = chats;
        this.events = events;
    }

    @Transactional
    public ChatSession createSession(@NotNull UUID chatId, @NotNull User initiator, @NotNull String publicKey) {
        Chat chat = chats.getChatById(chatId);

        if (!chat.involvesUser(initiator))
            throw new ChatService.ChatNotFoundException(chatId);

        ChatSession session = new ChatSession();
        session.setChat(chat);
        session.setInitiator(initiator);
        session.setResponder(chat.getOtherUser(initiator));
        sessions.save(session);

        ChatSessionInitiationEvent event = new ChatSessionInitiationEvent(
            session.getId(),
            chat.getId(),
            chat.getDhGroup(),
            publicKey
        );
        events.publish(session.getResponder(), event);

        return session;
    }

    public ChatSession getSessionById(UUID sessionId) {
        return sessions.findById(sessionId)
            .orElseThrow(() -> new ChatSessionNotFoundException(sessionId));
    }

    @Transactional
    public void establishSession(@NotNull UUID sessionId, @NotNull User responder, @NotNull String publicKey) {
        ChatSession session = getSessionById(sessionId);

        if (session.isEstablished())
            throw new ChatSessionAlreadyEstablishedException(sessionId);
        if (!session.isResponder(responder))
            throw new UserCannotRespondException(sessionId, responder.getId());

        session.setEstablished(true);
        sessions.save(session);

        UUID chatId = session.getChat().getId();
        ChatSessionResponseEvent event = new ChatSessionResponseEvent(sessionId, chatId, publicKey);
        events.publish(session.getInitiator(), event);
    }

    @Transactional
    public void deleteSession(UUID sessionId, User user) {
        ChatSession session = getSessionById(sessionId);

        if (!session.involvesUser(user))
            throw new UserNotInvolvedException(sessionId, user.getId());

        sessions.delete(session);

        UUID chatId = session.getChat().getId();
        ChatSessionRemovedEvent event = new ChatSessionRemovedEvent(sessionId, chatId);
        events.publish(session.getInitiator(), event);
        events.publish(session.getResponder(), event);
    }

    public static final class ChatSessionNotFoundException extends RuntimeException {

        public ChatSessionNotFoundException(UUID sessionId) {
            super("Chat session \"%s\" does not exist".formatted(sessionId));
        }
    }

    public static final class ChatSessionAlreadyEstablishedException extends RuntimeException {

        public ChatSessionAlreadyEstablishedException(UUID sessionId) {
            super("Chat session \"%s\" is already established".formatted(sessionId));
        }
    }

    public static final class UserCannotRespondException extends RuntimeException {

        public UserCannotRespondException(UUID sessionId, UUID userId) {
            super("User \"%s\" cannot respond in session \"%s\"".formatted(sessionId, userId));
        }
    }

    public static final class UserNotInvolvedException extends RuntimeException {

        public UserNotInvolvedException(UUID sessionId, UUID userId) {
            super("User \"%s\" is not involved in session \"%s\"".formatted(sessionId, userId));
        }
    }
}
