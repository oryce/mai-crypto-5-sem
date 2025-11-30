package dora.messenger.client.persistence;

import dora.messenger.client.store.chat.Chat.DiffieHellmanGroupId;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static dora.messenger.client.persistence.TypeMapping.uuidToBytes;

public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    private final Connection connection;

    public ChatSessionRepositoryImpl(@NotNull Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    public void init() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS `chat_sessions` (
                    `session_id`      BLOB PRIMARY KEY,
                    `chat_id`         BLOB NOT NULL,
                    `secret`          BLOB NOT NULL,
                    `dh_group_id`     VARCHAR(64) NOT NULL,
                    `shared_secret`   BLOB
                );
                """
            );

            statement.executeUpdate(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS `idx_chat_sessions_chat_id_unique`
                    ON `chat_sessions` (`chat_id`);
                """
            );
        } catch (SQLException e) {
            throw new RuntimeException("Cannot initialize session table", e);
        }
    }

    @Override
    public Optional<ChatSession> findByChatId(UUID chatId) {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            SELECT `session_id`, `chat_id`, `dh_group_id`, `secret`, `shared_secret`
            FROM `chat_sessions`
            WHERE `chat_id` = ?
            """
        )) {
            statement.setBytes(1, uuidToBytes(chatId));

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapResultSetToChatSession(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot execute `findByChatId` query", e);
        }
    }

    private Optional<ChatSession> mapResultSetToChatSession(ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) return Optional.empty();

        var session = new ChatSession();
        session.setSessionId(TypeMapping.uuidFromBytes(resultSet.getBytes(1)));
        session.setChatId(TypeMapping.uuidFromBytes(resultSet.getBytes(2)));
        session.setDhGroupId(DiffieHellmanGroupId.valueOf(resultSet.getString(3)));
        session.setPrivateKey(resultSet.getBytes(4));

        var sharedSecret = resultSet.getBytes(5);
        session.setSharedSecret(sharedSecret);

        return Optional.of(session);
    }

    @Override
    public void save(ChatSession chatSession) {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            INSERT INTO `chat_sessions` (`session_id`, `chat_id`, `dh_group_id`, `secret`, `shared_secret`)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(`chat_id`) DO UPDATE SET
                `session_id`        = excluded.`session_id`,
                `dh_group_id`       = excluded.`dh_group_id`,
                `secret`            = excluded.`secret`,
                `shared_secret`     = excluded.`shared_secret`
            """
        )) {
            statement.setBytes(1, uuidToBytes(chatSession.getSessionId()));
            statement.setBytes(2, uuidToBytes(chatSession.getChatId()));
            statement.setString(3, chatSession.getDhGroupId().name());
            statement.setBytes(4, chatSession.getPrivateKey());

            byte[] sharedSecret = chatSession.getSharedSecret();
            if (sharedSecret == null) {
                statement.setNull(5, Types.BLOB);
            } else {
                statement.setBytes(5, sharedSecret);
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot execute `save` query", e);
        }
    }

    @Override
    public void deleteByChatId(UUID chatId) {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            DELETE FROM `chat_sessions`
            WHERE `chat_id` = ?
            """
        )) {
            statement.setBytes(1, uuidToBytes(chatId));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot execute `deleteByChatId` query", e);
        }
    }
}
