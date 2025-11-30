package dora.messenger.server.user;

import dora.messenger.protocol.contact.ContactDto;
import dora.messenger.server.contact.Contact;
import dora.messenger.server.contact.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User")
@RestController
@RequestMapping("/users")
public class UserContactController {

    private final ContactService contacts;
    private final Contact.Mapper contactMapper;

    public UserContactController(ContactService contacts, Contact.Mapper contactMapper) {
        this.contacts = contacts;
        this.contactMapper = contactMapper;
    }

    @Operation(summary = "Get Contacts")
    @GetMapping("/@self/contacts")
    public List<ContactDto> getContacts(@AuthenticationPrincipal User user) {
        return contacts.getContacts(user).stream()
            .map((contact) -> contactMapper.toDto(contact, contact.getOtherUser(user)))
            .toList();
    }
}
