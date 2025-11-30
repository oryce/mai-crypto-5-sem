package dora.messenger.server.user;

import dora.messenger.protocol.contact.ContactRequestDto;
import dora.messenger.server.contact.ContactRequest;
import dora.messenger.server.contact.ContactRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

@Tag(name = "User")
@RestController
@RequestMapping("/users")
public class UserContactRequestController {

    private final ContactRequestService requests;
    private final ContactRequest.Mapper requestMapper;

    public UserContactRequestController(ContactRequestService requests, ContactRequest.Mapper requestMapper) {
        this.requests = requests;
        this.requestMapper = requestMapper;
    }

    @Operation(summary = "Get Contact Requests")
    @GetMapping("/@self/contact-requests")
    public List<ContactRequestDto> getContactRequests(@AuthenticationPrincipal User user) {
        Stream<ContactRequestDto> incomingRequests = requests.getIncomingRequests(user)
            .stream()
            .map((request) -> requestMapper.toIncomingDto(request, request.getInitiator()));

        Stream<ContactRequestDto> outgoingRequests = requests.getOutgoingRequests(user)
            .stream()
            .map((request) -> requestMapper.toOutgoingDto(request, request.getResponder()));

        return Stream.concat(incomingRequests, outgoingRequests).toList();
    }
}
