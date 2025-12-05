package dora.messenger.server.user;

import dora.messenger.protocol.session.SessionCredentialsDto;
import dora.messenger.protocol.user.CreateUserRequest;
import dora.messenger.protocol.user.CreateUserResponse;
import dora.messenger.protocol.user.UserDto;
import dora.messenger.server.session.SessionCredentials;
import dora.messenger.server.user.UserService.RegisterResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create User")
    @PostMapping("/users")
    public CreateUserResponse createUser(@RequestBody @Validated CreateUserRequest request) {
        RegisterResult result = userService.registerUser(
            request.firstName(),
            request.lastName(),
            request.username(),
            request.password()
        );

        User user = result.user();
        SessionCredentials credentials = result.credentials();

        return new CreateUserResponse(
            new UserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getUsername()),
            new SessionCredentialsDto(credentials.accessToken())
        );
    }
}
