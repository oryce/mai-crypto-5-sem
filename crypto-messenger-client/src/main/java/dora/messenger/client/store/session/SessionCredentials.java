package dora.messenger.client.store.session;

import dora.messenger.protocol.session.SessionCredentialsDto;

public record SessionCredentials(String accessToken) {

    public static SessionCredentials from(SessionCredentialsDto credentialsDto) {
        return new SessionCredentials(credentialsDto.accessToken());
    }
}
