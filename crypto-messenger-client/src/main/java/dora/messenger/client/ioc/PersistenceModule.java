package dora.messenger.client.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dora.messenger.client.persistence.ChatFileRepository;
import dora.messenger.client.persistence.ChatFileRepositoryImpl;
import dora.messenger.client.persistence.ChatMessageRepository;
import dora.messenger.client.persistence.ChatMessageRepositoryImpl;
import dora.messenger.client.persistence.ChatSessionRepository;
import dora.messenger.client.persistence.ChatSessionRepositoryImpl;
import dora.messenger.client.persistence.PropertyRepository;
import dora.messenger.client.persistence.PropertyRepositoryImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PersistenceModule extends AbstractModule {

    @Provides
    @Singleton
    public Connection connection() throws SQLException {
        String connectionString = "jdbc:sqlite:messenger.db";
        return DriverManager.getConnection(connectionString);
    }

    @Provides
    @Singleton
    public ChatFileRepository chatFiles(Connection connection) {
        var chatSessions = new ChatFileRepositoryImpl(connection);
        chatSessions.init();
        return chatSessions;
    }

    @Provides
    @Singleton
    public ChatMessageRepository chatMessages(Connection connection) {
        var chatSessions = new ChatMessageRepositoryImpl(connection);
        chatSessions.init();
        return chatSessions;
    }

    @Provides
    @Singleton
    public ChatSessionRepository chatSessions(Connection connection) {
        var chatSessions = new ChatSessionRepositoryImpl(connection);
        chatSessions.init();
        return chatSessions;
    }

    @Provides
    @Singleton
    public PropertyRepository properties(Connection connection) {
        var properties = new PropertyRepositoryImpl(connection);
        properties.init();
        return properties;
    }
}
