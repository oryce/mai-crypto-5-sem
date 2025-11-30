package dora.messenger.server.chat;

import dora.messenger.protocol.chat.ChatDto;
import dora.messenger.protocol.chat.CreateChat;
import dora.messenger.server.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Chat")
@RestController
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chats;
    private final Chat.Mapper mapper;

    public ChatController(ChatService chats, Chat.Mapper mapper) {
        this.chats = chats;
        this.mapper = mapper;
    }

    @Operation(summary = "Create Chat")
    @PostMapping
    public ChatDto createChat(
        @AuthenticationPrincipal User initiator,
        @RequestBody @Validated CreateChat createChat
    ) {
        Chat chat = chats.createChat(
            initiator,
            createChat.participantId(),
            mapper.dhGroupFromDto(createChat.dhGroup()),
            mapper.algorithmFromDto(createChat.algorithm()),
            mapper.cipherModeFromDto(createChat.encryptionMode()),
            mapper.paddingFromDto(createChat.paddingMode())
        );

        return mapper.toDto(chat, chat.getName(initiator));
    }

    @Operation(summary = "Delete Chat")
    @DeleteMapping("/{chatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChat(
        @PathVariable("chatId") UUID chatId,
        @AuthenticationPrincipal User user
    ) {
        chats.deleteChat(chatId, user);
    }
}
