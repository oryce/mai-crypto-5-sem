package dora.messenger.client.persistence;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository {

    List<ChatMessage> findAllByChatId(UUID chatId);

    void save(ChatMessage message);
}
