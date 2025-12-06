package dora.messenger.server.contact;

import dora.messenger.server.user.User;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contacts;

    @Transactional
    public Contact createContact(
        @Validated
        @NotNull(message = "First user may not be null")
        User firstUser,

        @Validated
        @NotNull(message = "Second user may not be null")
        User secondUser
    ) {
        if (contacts.existsByFirstUserAndSecondUser(firstUser, secondUser))
            throw new ContactAlreadyExistsException(firstUser.getId(), secondUser.getId());

        Contact contact = new Contact();
        contact.setFirstUser(firstUser);
        contact.setSecondUser(secondUser);

        return contacts.save(contact);
    }

    public Contact getById(UUID contactId) {
        return contacts.findById(contactId)
            .orElseThrow(() -> new ContactNotFoundException(contactId));
    }

    public List<Contact> getContacts(User user) {
        return contacts.findAllByUser(user);
    }

    @Transactional
    public void deleteContact(UUID contactId, User user) {
        Contact contact = getById(contactId);

        if (!contact.involvesUser(user))
            throw new UserNotInvolvedException(contactId, user.getId());

        contacts.delete(contact);
    }

    public static final class ContactAlreadyExistsException extends RuntimeException {

        public ContactAlreadyExistsException(UUID firstUserId, UUID secondUserId) {
            super("Users \"%s\" and \"%s\" already have a contact established".formatted(firstUserId, secondUserId));
        }
    }

    public static final class ContactNotFoundException extends RuntimeException {

        public ContactNotFoundException(UUID contactId) {
            super("Contact \"%s\" does not exist".formatted(contactId));
        }
    }

    public static final class UserNotInvolvedException extends RuntimeException {

        public UserNotInvolvedException(UUID contactId, UUID participantId) {
            super("User \"%s\" is not involved in contact \"%s\"".formatted(participantId, contactId));
        }
    }
}
