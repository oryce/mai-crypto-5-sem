package dora.messenger.server.user;

import dora.messenger.server.session.Session;
import dora.messenger.server.session.SessionCredentials;
import dora.messenger.server.session.SessionService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public User createUser(
        @Validated
        @Size(min = 1, max = 32)
        @NotBlank
        String firstName,

        @Validated
        @Size(min = 1, max = 32)
        @NotBlank
        String lastName,

        @Validated
        @Pattern(regexp = "\\w{3,16}", message = "Username must contain 3-16 alphanumeric characters")
        String username,

        @Validated
        @Pattern(regexp = "\\S{8,128}", message = "Password must contain 8-128 non-blank characters")
        String password
    ) {
        User user = new User();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        return users.save(user);
    }

    @Transactional
    public RegisterResult registerUser(
        String firstName,
        String lastName,
        String username,
        String password
    ) {
        User user = createUser(firstName, lastName, username, password);
        Session session = sessionService.createSession(user);
        SessionCredentials credentials = sessionService.createCredentials(session);

        return new RegisterResult(user, credentials);
    }

    public record RegisterResult(User user, SessionCredentials credentials) {
    }
}
