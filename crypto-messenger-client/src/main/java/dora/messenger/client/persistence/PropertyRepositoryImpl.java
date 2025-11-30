package dora.messenger.client.persistence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PropertyRepositoryImpl implements PropertyRepository {

    private final Connection connection;

    public PropertyRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    public void init() {
        try {
            Statement statement = connection.createStatement();

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS properties (
                    property_key VARCHAR(64) NOT NULL,
                    %s,
                    PRIMARY KEY (property_key)
                )
                """
                    .formatted(valueColumns())
            );
        } catch (SQLException e) {
            throw new RuntimeException("Cannot initialize property table", e);
        }
    }

    private String valueColumns() {
        List<Type<?>> types = List.of(Type.STRING);

        return types.stream()
            .map((type) -> "%s %s".formatted(type.column(), type.type()))
            .collect(Collectors.joining(",\n"));
    }

    @Override
    public Optional<String> getString(PropertyKey key) {
        return get(Type.STRING, key);
    }

    @Override
    public void setString(PropertyKey key, String value) {
        set(Type.STRING, key, value);
    }

    private <T> Optional<T> get(Type<T> type, PropertyKey key) {
        try {
            //noinspection SqlSourceToSinkFlow cannot use `?` in column names
            PreparedStatement statement = connection.prepareStatement(
                "SELECT %s FROM properties WHERE property_key = ?".formatted(type.column())
            );
            statement.setString(1, key.name());

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? Optional.of(type.get(resultSet, 1)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot execute \"get\" query", e);
        }
    }

    private <T> void set(Type<T> type, PropertyKey key, @Nullable T value) {
        if (value != null) {
            update(type, key, value);
        } else {
            delete(key);
        }
    }

    private <T> void update(Type<T> type, PropertyKey key, @NotNull T value) {
        try {
            String column = type.column();

            //noinspection SqlSourceToSinkFlow cannot use `?` in column names
            PreparedStatement statement = connection.prepareStatement(
                """
                INSERT INTO properties (property_key, %s)
                VALUES (?, ?)
                ON CONFLICT(property_key) DO UPDATE SET %s = ?
                """.formatted(column, column)
            );

            statement.setString(1, key.name());
            type.set(statement, 2, value);
            type.set(statement, 3, value);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot execute \"update\" query", e);
        }
    }

    private <T> void delete(PropertyKey key) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM properties WHERE property_key = ?");
            statement.setString(1, key.name());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot execute \"delete\" query", e);
        }
    }

    private interface Type<T> {

        Type<String> STRING = new StringType();

        /**
         * Returns the type's column name.
         */
        String column();

        /**
         * Returns the type's SQL type.
         */
        String type();

        /**
         * Retrieves the type from a {@link ResultSet} at index <code>index</code>.
         */
        T get(ResultSet resultSet, int index) throws SQLException;

        /**
         * Sets the parameter at index <code>index</code> of the type.
         */
        void set(PreparedStatement statement, int index, T value) throws SQLException;
    }

    private static class StringType implements Type<String> {

        @Override
        public String column() {
            return "string_value";
        }

        @Override
        public String type() {
            return "VARCHAR(1024) NOT NULL";
        }

        @Override
        public String get(ResultSet resultSet, int index) throws SQLException {
            return resultSet.getString(index);
        }

        @Override
        public void set(PreparedStatement statement, int index, String value) throws SQLException {
            statement.setString(index, value);
        }
    }
}
