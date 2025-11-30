package dora.messenger.client.persistence;

import java.util.Optional;
import java.util.UUID;

public interface ChatFileRepository {

    Optional<ChatFile> findById(UUID id);

    void save(ChatFile chatFile);
}
