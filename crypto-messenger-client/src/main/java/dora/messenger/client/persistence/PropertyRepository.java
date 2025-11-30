package dora.messenger.client.persistence;

import java.util.Optional;

public interface PropertyRepository {

    Optional<String> getString(PropertyKey key);

    void setString(PropertyKey key, String value);
}
