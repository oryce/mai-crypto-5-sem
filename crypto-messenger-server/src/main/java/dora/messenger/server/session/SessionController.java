package dora.messenger.server.session;

import dora.messenger.protocol.session.CreateSessionRequest;
import dora.messenger.protocol.session.CreateSessionResponse;
import dora.messenger.protocol.session.SessionCredentialsDto;
import dora.messenger.protocol.session.SessionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Session")
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "Create Session")
    @PostMapping
    public CreateSessionResponse createSession(CreateSessionRequest request) {
        Session session = sessionService.createSession(request.username(), request.password());
        SessionCredentials credentials = sessionService.createCredentials(session);

        return new CreateSessionResponse(
            new SessionDto(session.getId()),
            new SessionCredentialsDto(credentials.accessToken())
        );
    }

    @Operation(summary = "Invalidate Current Session")
    @DeleteMapping("/@current")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invalidateCurrentSession() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof SessionAuthenticationToken sessionAuth))
            throw new IllegalStateException("Unsupported authentication");

        Session session = sessionAuth.getSession();
        sessionService.invalidateSession(session);
    }
}
