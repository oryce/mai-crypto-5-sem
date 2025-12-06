package dora.messenger.server.contact;

import dora.messenger.server.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactRequestRepository extends JpaRepository<ContactRequest, UUID> {

    List<ContactRequest> findAllByInitiator(User initiator);

    List<ContactRequest> findAllByResponder(User responder);

    @Query(
        """
        SELECT CASE WHEN COUNT(request) > 0 THEN true ELSE false END
        FROM ContactRequest request
        WHERE (request.initiator=:initiator AND request.responder=:responder) OR
              (request.initiator=:responder AND request.responder=:initiator)
        """
    )
    boolean existsByInitiatorAndResponder(@Param("initiator") User initiator, @Param("responder") User responder);
}
