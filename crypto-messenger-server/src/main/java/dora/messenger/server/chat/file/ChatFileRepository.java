package dora.messenger.server.chat.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatFileRepository extends JpaRepository<ChatFile, UUID> {

    Optional<ChatFile> findByIdAndSessionId(UUID id, UUID sessionId);

    List<ChatFile> findByIdInAndSessionId(Collection<UUID> ids, UUID sessionId);
}
