package dora.messenger.server.chat;

import dora.messenger.server.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    @Query("SELECT chat from Chat chat WHERE chat.firstUser = :user OR chat.secondUser = :user")
    List<Chat> findAllByUser(@Param("user") User user);

    @Query(
        """
        SELECT CASE WHEN COUNT(chat) > 0 THEN true ELSE false END
        FROM Chat chat
        WHERE (chat.firstUser=:firstUser AND chat.secondUser=:secondUser) OR
              (chat.secondUser=:firstUser AND chat.firstUser=:secondUser)
        """
    )
    boolean existsByFirstUserAndSecondUser(
        @Param("firstUser") User firstUser,
        @Param("secondUser") User secondUser
    );
}
