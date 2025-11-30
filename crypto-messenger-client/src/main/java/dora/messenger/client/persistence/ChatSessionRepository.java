package dora.messenger.client.persistence;

import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository {

    Optional<ChatSession> findByChatId(UUID chatId);

    void save(ChatSession chatSession);

    void deleteByChatId(UUID chatId);
}
