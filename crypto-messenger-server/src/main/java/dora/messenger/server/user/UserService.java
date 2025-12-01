package dora.messenger.server.user;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public void createUser(
        @Validated
        @Pattern(regexp = "\\w{3,16}", message = "Username must contain 3-16 alphanumeric characters")
        String username,

        @Validated
        @Pattern(regexp = "\\S{8,128}", message = "Password must contain 8-128 non-blank characters")
        String password
    ) {
        User user = new User();

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        users.save(user);
    }
}
