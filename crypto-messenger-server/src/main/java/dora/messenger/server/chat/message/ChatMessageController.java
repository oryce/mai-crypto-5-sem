package dora.messenger.server.chat.message;

import dora.messenger.protocol.chat.message.ChatMessageDto;
import dora.messenger.protocol.chat.message.SendChatMessage;
import dora.messenger.server.chat.Blob;
import dora.messenger.server.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Chat")
@RestController
@RequestMapping("/chats")
public class ChatMessageController {

    private final ChatMessageService messages;
    private final ChatMessage.Mapper messageMapper;
    private final Blob.Mapper blobMapper;

    public ChatMessageController(
        ChatMessageService messages,
        ChatMessage.Mapper messageMapper,
        Blob.Mapper blobMapper
    ) {
        this.messages = messages;
        this.messageMapper = messageMapper;
        this.blobMapper = blobMapper;
    }

    @Operation(summary = "Send Chat Message")
    @PostMapping("/sessions/{sessionId}/messages")
    public ChatMessageDto sendMessage(
        @PathVariable("sessionId") UUID sessionId,
        @AuthenticationPrincipal User sender,
        @RequestBody @Validated SendChatMessage sendMessage
    ) {
        ChatMessage message = messages.sendMessage(
            sessionId,
            sender,
            blobMapper.map(sendMessage.content()),
            sendMessage.attachments()
        );
        return messageMapper.map(message);
    }
}
