package dora.messenger.server.user;

import dora.messenger.protocol.user.CreateUser;
import dora.messenger.protocol.user.CreatedUser;
import dora.messenger.protocol.user.UserDto;
import dora.messenger.server.session.SessionCredentials;
import dora.messenger.server.user.UserService.RegisterResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService users;
    private final User.Mapper userMapper;
    private final SessionCredentials.Mapper credentialsMapper;

    public UserController(
        UserService users,
        User.Mapper userMapper,
        SessionCredentials.Mapper credentialsMapper
    ) {
        this.users = users;
        this.userMapper = userMapper;
        this.credentialsMapper = credentialsMapper;
    }

    @Operation(summary = "Create User")
    @PostMapping
    public CreatedUser createUser(@RequestBody @Validated CreateUser createRequest) {
        RegisterResult result = users.registerUser(
            createRequest.firstName(),
            createRequest.lastName(),
            createRequest.username(),
            createRequest.password()
        );

        return new CreatedUser(
            userMapper.toDto(result.user()),
            credentialsMapper.toDto(result.credentials())
        );
    }

    @Operation(summary = "Get User")
    @GetMapping("/@self")
    public UserDto getUser(@AuthenticationPrincipal User user) {
        return userMapper.toDto(user);
    }
}
