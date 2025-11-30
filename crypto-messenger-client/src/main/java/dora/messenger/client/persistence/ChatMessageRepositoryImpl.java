package dora.messenger.client.persistence;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dora.messenger.client.persistence.TypeMapping.uuidFromBytes;
import static dora.messenger.client.persistence.TypeMapping.uuidToBytes;
import static java.util.Objects.requireNonNull;

public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final Connection connection;

    public ChatMessageRepositoryImpl(@NotNull Connection connection) {
        this.connection = requireNonNull(connection, "connection");
    }

    public void init() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS `chat_messages` (
                    `id`                    BLOB PRIMARY KEY,
                    `chat_id`               BLOB NOT NULL,
                    `session_id`            BLOB NOT NULL,
                    `sender_id`             BLOB NOT NULL,
                    `timestamp`             INTEGER NOT NULL,
                    `content`               TEXT NOT NULL
                );
                """
            );

            statement.executeUpdate(
                """
                CREATE INDEX IF NOT EXISTS `idx_chat_messages_chat_id` ON `chat_messages` (`chat_id`);
                """
            );

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS `chat_message_attachments` (
                    `message_id`        BLOB NOT NULL,
                    `attachment_id`     BLOB NOT NULL,
                    PRIMARY KEY (`message_id`, `attachment_id`)
                );
                """
            );

            statement.executeUpdate(
                """
                CREATE INDEX IF NOT EXISTS `idx_chat_message_attachments_message_id`
                    ON `chat_message_attachments` (`message_id`);
                """
            );
        } catch (SQLException e) {
            throw new RuntimeException("Cannot initialize messages table", e);
        }
    }

    @Override
    public List<ChatMessage> findAllByChatId(UUID chatId) {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            SELECT `id`, `chat_id`, `session_id`, `sender_id`, `timestamp`, `content`
            FROM `chat_messages`
            WHERE `chat_id` = ?
            ORDER BY `timestamp`
            """
        )) {
            statement.setBytes(1, uuidToBytes(chatId));

            try (ResultSet resultSet = statement.executeQuery()) {
                List<ChatMessage> chatMessages = new ArrayList<>();
                Map<UUID, ChatMessage> messagesById = new HashMap<>();

                while (resultSet.next()) {
                    ChatMessage message = mapMessage(resultSet);
                    chatMessages.add(message);
                    messagesById.put(message.getId(), message);
                }

                populateAttachmentIds(chatId, messagesById);
                return chatMessages;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot execute `findAllByChatId` query", e);
        }
    }

    @Override
    public void save(ChatMessage message) {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            INSERT INTO `chat_messages`
                (`id`, `chat_id`, `session_id`, `sender_id`, `timestamp`, `content`)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(`id`) DO UPDATE SET
                `chat_id`               = excluded.`chat_id`,
                `session_id`            = excluded.`session_id`,
                `sender_id`             = excluded.`sender_id`,
                `timestamp`             = excluded.`timestamp`,
                `content`               = excluded.`content`
            """
        )) {
            statement.setBytes(1, uuidToBytes(message.getId()));
            statement.setBytes(2, uuidToBytes(message.getChatId()));
            statement.setBytes(3, uuidToBytes(message.getSessionId()));
            statement.setBytes(4, uuidToBytes(message.getSenderId()));
            statement.setLong(5, message.getTimestamp().getEpochSecond());
            statement.setString(6, message.getContent());

            statement.executeUpdate();
            saveAttachments(message);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot execute `save` query", e);
        }
    }

    private ChatMessage mapMessage(ResultSet resultSet) throws SQLException {
        ChatMessage message = new ChatMessage();

        message.setId(uuidFromBytes(resultSet.getBytes(1)));
        message.setChatId(uuidFromBytes(resultSet.getBytes(2)));
        message.setSessionId(uuidFromBytes(resultSet.getBytes(3)));
        message.setSenderId(uuidFromBytes(resultSet.getBytes(4)));
        message.setTimestamp(Instant.ofEpochSecond(resultSet.getLong(5)));
        message.setContent(resultSet.getString(6));
        message.setAttachmentIds(new ArrayList<>());

        return message;
    }

    private void populateAttachmentIds(UUID chatId, Map<UUID, ChatMessage> messagesById) {
        if (messagesById.isEmpty()) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(
            """
            SELECT `cma`.`message_id`, `cma`.`attachment_id`
            FROM `chat_message_attachments` AS `cma`
            JOIN `chat_messages` AS `cm` ON `cm`.`id` = `cma`.`message_id`
            WHERE `cm`.`chat_id` = ?
            ORDER BY `cma`.`message_id`
            """
        )) {
            statement.setBytes(1, uuidToBytes(chatId));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    UUID messageId = uuidFromBytes(resultSet.getBytes(1));
                    UUID attachmentId = uuidFromBytes(resultSet.getBytes(2));

                    ChatMessage message = messagesById.get(messageId);
                    if (message == null) continue;

                    message.getAttachmentIds().add(attachmentId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot execute `populateAttachmentIds` query", e);
        }
    }

    private void saveAttachments(ChatMessage message) throws SQLException {
        UUID messageId = message.getId();
        List<UUID> attachmentIds = message.getAttachmentIds();

        try (PreparedStatement statement = connection.prepareStatement(
            """
            DELETE FROM `chat_message_attachments` WHERE `message_id` = ?
            """
        )) {
            statement.setBytes(1, uuidToBytes(messageId));
            statement.executeUpdate();
        }

        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(
            """
            INSERT INTO `chat_message_attachments` (`message_id`, `attachment_id`)
            VALUES (?, ?)
            """
        )) {
            for (UUID attachmentId : attachmentIds) {
                statement.setBytes(1, uuidToBytes(messageId));
                statement.setBytes(2, uuidToBytes(attachmentId));
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }
}
