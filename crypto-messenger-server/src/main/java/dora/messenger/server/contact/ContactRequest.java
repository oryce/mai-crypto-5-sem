package dora.messenger.server.contact;

import dora.messenger.protocol.contact.ContactRequestDto;
import dora.messenger.server.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.UUID;

@Entity
@Table(
    name = "contact_requests",
    indexes = {
        @Index(name = "idx_contact_request_initiator_id", columnList = "initiator_id"),
        @Index(name = "idx_contact_request_responder_id", columnList = "responder_id"),
    },
    uniqueConstraints = @UniqueConstraint(
        name = "constraint_contact_request_initiator_id_responder_id",
        columnNames = { "initiator_id", "responder_id" }
    )
)
public class ContactRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "initiator_id")
    @ManyToOne
    private User initiator;

    @JoinColumn(name = "responder_id")
    @ManyToOne
    private User responder;

    public boolean canBeCancelledBy(User user) {
        return user.getId().equals(initiator.getId());
    }

    public boolean canBeRespondedBy(User user) {
        return user.getId().equals(responder.getId());
    }

    //region Accessors
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getInitiator() {
        return initiator;
    }

    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }

    public User getResponder() {
        return responder;
    }

    public void setResponder(User responder) {
        this.responder = responder;
    }
    //endregion

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING, uses = { User.Mapper.class })
    public interface Mapper {

        @Mapping(target = "id", source = "request.id")
        @Mapping(target = "direction", source = "direction")
        @Mapping(target = "user", source = "otherUser")
        ContactRequestDto toDto(ContactRequest request, ContactRequestDto.Direction direction, User otherUser);

        default ContactRequestDto toIncomingDto(ContactRequest request, User otherUser) {
            return toDto(request, ContactRequestDto.Direction.INCOMING, otherUser);
        }

        default ContactRequestDto toOutgoingDto(ContactRequest request, User otherUser) {
            return toDto(request, ContactRequestDto.Direction.OUTGOING, otherUser);
        }
    }
}
