package dora.messenger.server.chat.message;

import dora.messenger.server.chat.Blob;
import dora.messenger.server.chat.Chat;
import dora.messenger.server.chat.file.ChatFile;
import dora.messenger.server.chat.file.ChatFileService;
import dora.messenger.server.chat.session.ChatSession;
import dora.messenger.server.chat.session.ChatSessionService;
import dora.messenger.server.event.EventService;
import dora.messenger.server.user.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
public class ChatMessageService {

    private final ChatSessionService sessions;
    private final ChatFileService files;
    private final EventService events;

    public ChatMessageService(
        ChatSessionService sessions,
        ChatFileService files,
        EventService events
    ) {
        this.sessions = sessions;
        this.files = files;
        this.events = events;
    }

    public ChatMessage sendMessage(
        @NotNull UUID sessionId,
        @NotNull User sender,
        @NotNull Blob content,
        @NotNull List<UUID> attachmentIds
    ) {
        ChatSession session = sessions.getSessionById(sessionId);

        if (!session.involvesUser(sender))
            // TODO (19.12.25, ~oryce):
            //   Does it make sense to throw an exception from `ChatSessionService`?
            throw new ChatSessionService.UserNotInvolvedException(sessionId, sender.getId());

        Chat chat = session.getChat();

        // Resolve provided attachment IDs.
        List<ChatFile> attachments = files.resolveAll(attachmentIds, sessionId);

        if (attachments.size() != attachmentIds.size()) {
            // Find unresolved attachments.
            Collection<UUID> unresolvedIds = attachmentIds.stream()
                .filter((givenId) -> attachments.stream()
                    .noneMatch((resolved) -> resolved.getId().equals(givenId)))
                .toList();
            throw new UnknownFilesException(unresolvedIds);
        }

        ChatMessage message = new ChatMessage(
            /* message ID */ UUID.randomUUID(),
            chat.getId(),
            session.getId(),
            sender.getId(),
            /* timestamp */ Instant.now(),
            content,
            attachments
        );

        User recipient = chat.getOtherUser(sender);
        events.publish(recipient, new ChatMessageReceivedEvent(message));

        return message;
    }

    public static final class UnknownFilesException extends RuntimeException {

        public UnknownFilesException(Collection<UUID> unresolved) {
            super("Unknown files or not corresponding to this session: %s".formatted(
                unresolved.stream()
                    .map("\"%s\""::formatted)
                    .collect(Collectors.joining(", ")))
            );
        }
    }
}
