package dora.messenger.server.contact;

import dora.messenger.protocol.contact.ContactRequestDto;
import dora.messenger.protocol.contact.CreateContactRequest;
import dora.messenger.protocol.contact.UpdateContactRequest;
import dora.messenger.server.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Contact Requests")
@RestController
@RequestMapping("/contact-requests")
public class ContactRequestController {

    private final ContactRequestService requests;
    private final ContactRequest.Mapper requestMapper;

    public ContactRequestController(
        ContactRequestService requests,
        ContactRequest.Mapper requestMapper
    ) {
        this.requests = requests;
        this.requestMapper = requestMapper;
    }

    @Operation(summary = "Create Contact Request")
    @PostMapping
    public ContactRequestDto createRequest(
        @AuthenticationPrincipal User user,
        @RequestBody @Validated CreateContactRequest createRequest
    ) {
        ContactRequest request = requests.createRequest(user, createRequest.username());
        return requestMapper.toOutgoingDto(request, request.getResponder());
    }

    @Operation(summary = "Cancel Contact Request")
    @DeleteMapping("/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRequest(
        @AuthenticationPrincipal User initiator,
        @PathVariable("requestId") UUID requestId
    ) {
        requests.cancelRequest(requestId, initiator);
    }

    @Operation(summary = "Update Contact Request")
    @PatchMapping("/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRequest(
        @AuthenticationPrincipal User responder,
        @PathVariable("requestId") UUID requestId,
        @RequestBody @Validated UpdateContactRequest updateRequest
    ) {
        if (updateRequest.approved()) {
            requests.approveRequest(requestId, responder);
        } else {
            requests.rejectRequest(requestId, responder);
        }
    }
}
