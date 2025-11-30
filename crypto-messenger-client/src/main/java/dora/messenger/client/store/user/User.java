package dora.messenger.client.store.user;

import dora.messenger.protocol.user.UserDto;

import java.util.UUID;

public record User(
    UUID id,
    String firstName,
    String lastName,
    String username
) {

    public static User from(UserDto userDto) {
        return new User(
            userDto.id(),
            userDto.firstName(),
            userDto.lastName(),
            userDto.username()
        );
    }
}
