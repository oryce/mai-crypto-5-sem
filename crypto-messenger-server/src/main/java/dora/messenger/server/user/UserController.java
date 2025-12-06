package dora.messenger.server.user;

import dora.messenger.protocol.contact.ContactDto;
import dora.messenger.protocol.contact.ContactRequestDto;
import dora.messenger.protocol.user.CreateUser;
import dora.messenger.protocol.user.CreatedUser;
import dora.messenger.server.contact.Contact;
import dora.messenger.server.contact.ContactRequest;
import dora.messenger.server.contact.ContactRequestService;
import dora.messenger.server.contact.ContactService;
import dora.messenger.server.session.SessionCredentials;
import dora.messenger.server.user.UserService.RegisterResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

@Tag(name = "User")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService users;
    private final User.Mapper userMapper;
    private final SessionCredentials.Mapper credentialsMapper;

    private final ContactService contacts;
    private final Contact.Mapper contactMapper;

    private final ContactRequestService contactRequests;
    private final ContactRequest.Mapper contactRequestMapper;

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

    @Operation(summary = "Get Contacts")
    @GetMapping("/@self/contacts")
    public List<ContactDto> getContacts(@AuthenticationPrincipal User user) {
        return contacts.getContacts(user).stream()
            .map((contact) -> contactMapper.toDto(contact, contact.getOtherUser(user)))
            .toList();
    }

    @Operation(summary = "Get Contact Requests")
    @GetMapping("/@self/contact-requests")
    public List<ContactRequestDto> getContactRequests(@AuthenticationPrincipal User user) {
        Stream<ContactRequestDto> incomingRequests = contactRequests.getIncomingRequests(user)
            .stream()
            .map((request) -> contactRequestMapper.toIncomingDto(request, request.getInitiator()));

        Stream<ContactRequestDto> outgoingRequests = contactRequests.getOutgoingRequests(user)
            .stream()
            .map((request) -> contactRequestMapper.toOutgoingDto(request, request.getResponder()));

        return Stream.concat(incomingRequests, outgoingRequests).toList();
    }
}
