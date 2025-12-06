package dora.messenger.server.user;

import dora.messenger.protocol.user.UserDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.UUID;

@Entity
@Table(
    name = "users",
    indexes = @Index(name = "idx_user_username_unique", columnList = "username", unique = true)
)
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING)
    public interface Mapper {

        UserDto toDto(User user);
    }
}
