package dora.messenger.server.chat;

import dora.messenger.server.contact.ContactService;
import dora.messenger.server.event.EventService;
import dora.messenger.server.user.User;
import dora.messenger.server.user.UserService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Service
@Validated
public class ChatService {

    private final ChatRepository chats;
    private final UserService users;
    private final ContactService contacts;
    private final EventService events;

    public ChatService(
        ChatRepository chats,
        UserService users,
        ContactService contacts,
        EventService events
    ) {
        this.chats = chats;
        this.users = users;
        this.contacts = contacts;
        this.events = events;
    }

    @Transactional
    public Chat createChat(
        @NotNull User initiator,
        @NotNull UUID responderId,
        @NotNull Chat.DiffieHellmanGroup dhGroup,
        @NotNull Chat.Algorithm algorithm,
        @NotNull Chat.CipherMode encryptionMode,
        @NotNull Chat.Padding paddingMode
    ) {
        User responder = users.getById(responderId);

        if (!contacts.contactExists(initiator, responder))
            throw new UsersNotInContactException(initiator.getId(), responder.getId());

        Chat chat = new Chat();
        chat.setFirstUser(initiator);
        chat.setSecondUser(responder);
        chat.setDhGroup(dhGroup);
        chat.setAlgorithm(algorithm);
        chat.setCipherMode(encryptionMode);
        chat.setPadding(paddingMode);
        chats.save(chat);

        // Initiator creates the chat, responder receives an event.
        events.publish(responder, new ChatAddedEvent(chat, chat.getName(responder)));

        return chat;
    }

    public Chat getChatById(UUID chatId) {
        return chats.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(chatId));
    }

    public List<Chat> getChats(User user) {
        return chats.findAllByUser(user);
    }

    @Transactional
    public void deleteChat(UUID chatId, User user) {
        Chat chat = getChatById(chatId);

        if (!chat.involvesUser(user))
            throw new UserNotInvolvedException(chatId, user.getId());

        chats.delete(chat);

        User participant = chat.getOtherUser(user);
        events.publish(participant, new ChatRemovedEvent(chatId));
    }

    public static final class UsersNotInContactException extends RuntimeException {

        public UsersNotInContactException(UUID firstUserId, UUID secondUserId) {
            super("Users \"%s\" and \"%s\" are not in contacts".formatted(firstUserId, secondUserId));
        }
    }

    public static final class ChatNotFoundException extends RuntimeException {

        public ChatNotFoundException(UUID chatId) {
            super("Chat \"%s\" does not exist".formatted(chatId));
        }
    }

    public static final class UserNotInvolvedException extends RuntimeException {

        public UserNotInvolvedException(UUID chatId, UUID userId) {
            super("User \"%s\" is not involved in chat \"%s\"".formatted(userId, chatId));
        }
    }
}
