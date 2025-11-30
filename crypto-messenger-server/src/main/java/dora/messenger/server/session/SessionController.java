package dora.messenger.server.session;

import dora.messenger.protocol.session.CreateSession;
import dora.messenger.protocol.session.CreatedSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Session")
@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final Session.Mapper sessionMapper;
    private final SessionCredentials.Mapper credentialsMapper;

    public SessionController(
        SessionService sessionService,
        Session.Mapper sessionMapper,
        SessionCredentials.Mapper credentialsMapper
    ) {
        this.sessionService = sessionService;
        this.sessionMapper = sessionMapper;
        this.credentialsMapper = credentialsMapper;
    }

    @Operation(summary = "Create Session")
    @PostMapping
    public CreatedSession createSession(@RequestBody CreateSession createRequest) {
        Session session = sessionService.createSession(createRequest.username(), createRequest.password());
        SessionCredentials credentials = sessionService.createCredentials(session);
        return new CreatedSession(sessionMapper.toDto(session), credentialsMapper.toDto(credentials));
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
