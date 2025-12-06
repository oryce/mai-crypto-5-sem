package dora.messenger.server.session;

import dora.messenger.protocol.session.SessionCredentialsDto;
import org.jspecify.annotations.NonNull;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.Objects;

public record SessionCredentials(@NonNull String accessToken) {

    public SessionCredentials {
        Objects.requireNonNull(accessToken, "access token");
    }

    @org.mapstruct.Mapper(componentModel = ComponentModel.SPRING)
    public interface Mapper {

        SessionCredentialsDto toDto(SessionCredentials credentials);
    }
}
