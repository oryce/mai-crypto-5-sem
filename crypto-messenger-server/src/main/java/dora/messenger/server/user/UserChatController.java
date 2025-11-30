package dora.messenger.server.user;

import dora.messenger.protocol.chat.ChatDto;
import dora.messenger.server.chat.Chat;
import dora.messenger.server.chat.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User")
@RestController
@RequestMapping("/users")
public class UserChatController {

    private final ChatService chats;
    private final Chat.Mapper chatMapper;

    public UserChatController(ChatService chats, Chat.Mapper chatMapper) {
        this.chats = chats;
        this.chatMapper = chatMapper;
    }

    @Operation(summary = "Get Chats")
    @GetMapping("/@self/chats")
    public List<ChatDto> getChats(@AuthenticationPrincipal User user) {
        return chats.getChats(user).stream()
            .map((chat) -> chatMapper.toDto(chat, chat.getName(user)))
            .toList();
    }
}
