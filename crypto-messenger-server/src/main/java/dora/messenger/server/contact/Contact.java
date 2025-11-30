package dora.messenger.server.contact;

import dora.messenger.protocol.contact.ContactDto;
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
    name = "contacts",
    indexes = {
        @Index(name = "idx_contact_first_user_id", columnList = "first_user_id"),
        @Index(name = "idx_contact_second_user_id", columnList = "second_user_id"),
    },
    uniqueConstraints = @UniqueConstraint(
        name = "constraint_contact_first_user_id_second_user_id",
        columnNames = { "first_user_id", "second_user_id" }
    )
)
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "first_user_id")
    @ManyToOne
    private User firstUser;

    @JoinColumn(name = "second_user_id")
    @ManyToOne
    private User secondUser;

    public boolean involvesUser(User user) {
        return firstUser.getId().equals(user.getId())
            || secondUser.getId().equals(user.getId());
    }

    public User getOtherUser(User user) {
        if (firstUser.getId().equals(user.getId())) {
            return secondUser;
        } else if (secondUser.getId().equals(user.getId())) {
            return firstUser;
        } else {
            throw new IllegalArgumentException("User is not a contact participant");
        }
    }

    //region Accessors
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getFirstUser() {
        return firstUser;
    }

    public void setFirstUser(User firstUser) {
        this.firstUser = firstUser;
    }

    public User getSecondUser() {
        return secondUser;
    }

    public void setSecondUser(User secondUser) {
        this.secondUser = secondUser;
    }
    //endregion

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING, uses = { User.Mapper.class })
    public interface Mapper {

        @Mapping(target = "id", source = "contact.id")
        @Mapping(target = "user", source = "otherUser")
        ContactDto toDto(Contact contact, User otherUser);
    }
}
