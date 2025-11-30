package dora.messenger.server.contact;

import dora.messenger.server.event.EventService;
import dora.messenger.server.user.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Service
@Validated
public class ContactService {

    private final ContactRepository contacts;
    private final EventService events;

    public ContactService(ContactRepository contacts, EventService events) {
        this.contacts = contacts;
        this.events = events;
    }

    @Transactional
    public Contact createContact(@NotNull User firstUser, @NotNull User secondUser) {
        if (contacts.existsByFirstUserAndSecondUser(firstUser, secondUser))
            throw new ContactAlreadyExistsException(firstUser.getId(), secondUser.getId());

        Contact contact = new Contact();
        contact.setFirstUser(firstUser);
        contact.setSecondUser(secondUser);

        Contact savedContact = contacts.save(contact);

        events.publish(firstUser, new ContactAddedEvent(savedContact, secondUser));
        events.publish(secondUser, new ContactAddedEvent(savedContact, firstUser));

        return savedContact;
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

        User otherUser = contact.getOtherUser(user);
        events.publish(otherUser, new ContactRemovedEvent(contact));
    }

    public boolean contactExists(User firstUser, User secondUser) {
        return contacts.existsByFirstUserAndSecondUser(firstUser, secondUser);
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
