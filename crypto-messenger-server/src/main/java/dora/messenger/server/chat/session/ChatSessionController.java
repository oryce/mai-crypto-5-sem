package dora.messenger.server.chat.session;

import dora.messenger.protocol.chat.session.ChatSessionDto;
import dora.messenger.protocol.chat.session.CreateChatSession;
import dora.messenger.protocol.chat.session.EstablishChatSession;
import dora.messenger.server.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Chat")
@RestController
@RequestMapping("/chats")
public class ChatSessionController {

    private final ChatSessionService sessions;
    private final ChatSession.Mapper mapper;

    public ChatSessionController(ChatSessionService sessions, ChatSession.Mapper mapper) {
        this.sessions = sessions;
        this.mapper = mapper;
    }

    @Operation(summary = "Create Chat Session")
    @PostMapping("/{chatId}/session")
    public ChatSessionDto createSession(
        @PathVariable("chatId") UUID chatId,
        @AuthenticationPrincipal User initiator,
        @RequestBody @Validated CreateChatSession createSession
    ) {
        ChatSession session = sessions.createSession(chatId, initiator, createSession.publicKey());
        return mapper.toDto(session);
    }

    @Operation(summary = "Establish Chat Session")
    @PatchMapping("/sessions/{sessionId}")
    public void establishSession(
        @PathVariable("sessionId") UUID sessionId,
        @AuthenticationPrincipal User responder,
        @RequestBody @Validated EstablishChatSession establishSession
    ) {
        sessions.establishSession(sessionId, responder, establishSession.publicKey());
    }

    @Operation(summary = "Delete Chat Session")
    @DeleteMapping("/sessions/{sessionId}")
    public void deleteSession(
        @PathVariable("sessionId") UUID sessionId,
        @AuthenticationPrincipal User user
    ) {
        sessions.deleteSession(sessionId, user);
    }
}
