package dora.messenger.client.persistence;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

import static dora.messenger.client.persistence.TypeMapping.uuidFromBytes;
import static dora.messenger.client.persistence.TypeMapping.uuidToBytes;
import static java.util.Objects.requireNonNull;

public class ChatFileRepositoryImpl implements ChatFileRepository {

    private final Connection connection;

    public ChatFileRepositoryImpl(@NotNull Connection connection) {
        this.connection = requireNonNull(connection, "connection");
    }

    public void init() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS `chat_files` (
                    `id`            BLOB NOT NULL PRIMARY KEY,
                    `session_id`    BLOB NOT NULL,
                    `iv`            BLOB NOT NULL,
                    `filename`      TEXT NOT NULL,
                    `location`      TEXT
                );
                """
            );
        } catch (SQLException e) {
            throw new RuntimeException("Cannot initialize chat files table", e);
        }
    }

    @Override
    public Optional<ChatFile> findById(UUID id) {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            SELECT `id`, `session_id`, `iv`, `filename`, `location`
            FROM `chat_files`
            WHERE `id` = ?
            """
        )) {
            statement.setBytes(1, uuidToBytes(id));

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapChatFile(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot execute `findByIdAndSessionId` query", e);
        }
    }

    @Override
    public void save(ChatFile chatFile) {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            INSERT INTO `chat_files` (`id`, `session_id`, `iv`, `filename`, `location`)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(`id`) DO UPDATE SET
                `iv`            = excluded.`iv`,
                `session_id`    = excluded.`session_id`,
                `filename`      = excluded.`filename`,
                `location`      = excluded.`location`
            """
        )) {
            statement.setBytes(1, uuidToBytes(chatFile.getId()));
            statement.setBytes(2, uuidToBytes(chatFile.getSessionId()));
            statement.setBytes(3, chatFile.getIv());
            statement.setString(4, chatFile.getFilename());
            statement.setString(5, chatFile.getLocation());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot execute `save` query", e);
        }
    }

    private Optional<ChatFile> mapChatFile(ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) return Optional.empty();

        ChatFile chatFile = new ChatFile();
        chatFile.setId(uuidFromBytes(resultSet.getBytes(1)));
        chatFile.setSessionId(uuidFromBytes(resultSet.getBytes(2)));
        chatFile.setIv(resultSet.getBytes(3));
        chatFile.setFilename(resultSet.getString(4));
        chatFile.setLocation(resultSet.getString(5));

        return Optional.of(chatFile);
    }
}
