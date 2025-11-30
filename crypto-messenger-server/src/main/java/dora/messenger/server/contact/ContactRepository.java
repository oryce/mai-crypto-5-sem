package dora.messenger.server.contact;

import dora.messenger.server.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    @Query("SELECT contact FROM Contact contact WHERE contact.firstUser = :user OR contact.secondUser = :user")
    List<Contact> findAllByUser(@Param("user") User user);

    @Query(
        """
        SELECT CASE WHEN COUNT(contact) > 0 THEN true ELSE false END
        FROM Contact contact
        WHERE (contact.firstUser=:firstUser AND contact.secondUser=:secondUser) OR
              (contact.secondUser=:firstUser AND contact.firstUser=:secondUser)
        """
    )
    boolean existsByFirstUserAndSecondUser(
        @Param("firstUser") User firstUser,
        @Param("secondUser") User secondUser
    );
}
