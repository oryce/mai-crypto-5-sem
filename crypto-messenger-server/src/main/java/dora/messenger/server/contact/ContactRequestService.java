package dora.messenger.server.contact;

import dora.messenger.server.event.EventService;
import dora.messenger.server.user.User;
import dora.messenger.server.user.UserService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Service
@Validated
public class ContactRequestService {

    private final ContactRequestRepository requests;
    private final UserService users;
    private final ContactService contacts;
    private final EventService events;

    public ContactRequestService(
        ContactRequestRepository requests,
        UserService users,
        ContactService contacts,
        EventService events
    ) {
        this.requests = requests;
        this.users = users;
        this.contacts = contacts;
        this.events = events;
    }

    @Transactional
    public ContactRequest createRequest(@NotNull User initiator, @NotNull String responderUsername) {
        User responder = users.getByUsername(responderUsername);

        if (requests.existsByInitiatorAndResponder(initiator, responder))
            throw new RequestAlreadyExistsException(initiator.getId(), responder.getId());

        ContactRequest request = new ContactRequest();
        request.setInitiator(initiator);
        request.setResponder(responder);
        requests.save(request);

        // FIXME (17.12.25, ~oryce):
        //   If the transaction fails, the event is still published.
        events.publish(responder, new ContactRequestAddedEvent(request));

        return request;
    }

    public ContactRequest getById(UUID requestId) {
        return requests.findById(requestId)
            .orElseThrow(() -> new RequestNotFoundException(requestId));
    }

    public List<ContactRequest> getIncomingRequests(User user) {
        return requests.findAllByResponder(user);
    }

    public List<ContactRequest> getOutgoingRequests(User user) {
        return requests.findAllByInitiator(user);
    }

    @Transactional
    public void cancelRequest(UUID requestId, User initiator) {
        ContactRequest request = getById(requestId);

        if (!request.canBeCancelledBy(initiator))
            throw new UserCannotCancelException(requestId, initiator.getId());

        requests.delete(request);
        events.publish(request.getResponder(), new ContactRequestRemovedEvent(request));
    }

    @Transactional
    public void approveRequest(UUID requestId, User responder) {
        ContactRequest request = getById(requestId);

        if (!request.canBeRespondedBy(responder))
            throw new UserCannotRespondException(requestId, responder.getId());

        contacts.createContact(request.getInitiator(), request.getResponder());
        requests.delete(request);
        events.publish(request.getInitiator(), new ContactRequestRemovedEvent(request));
    }

    @Transactional
    public void rejectRequest(UUID requestId, User responder) {
        ContactRequest request = getById(requestId);

        if (!request.canBeRespondedBy(responder))
            throw new UserCannotRespondException(requestId, responder.getId());

        requests.delete(request);
        events.publish(request.getInitiator(), new ContactRequestRemovedEvent(request));
    }

    public static final class RequestAlreadyExistsException extends RuntimeException {

        public RequestAlreadyExistsException(UUID initiatorId, UUID responderId) {
            super("Contact request from \"%s\" to \"%s\" already exists".formatted(initiatorId, responderId));
        }
    }

    public static final class RequestNotFoundException extends RuntimeException {

        public RequestNotFoundException(UUID requestId) {
            super("Contact request \"%s\" does not exist".formatted(requestId));
        }
    }

    public static final class UserCannotCancelException extends RuntimeException {

        public UserCannotCancelException(UUID requestId, UUID initiatorId) {
            super("User \"%s\" cannot cancel request \"%s\"".formatted(requestId, initiatorId));
        }
    }

    public static final class UserCannotRespondException extends RuntimeException {

        public UserCannotRespondException(UUID requestId, UUID responderId) {
            super("User \"%s\" cannot respond to request \"%s\"".formatted(requestId, responderId));
        }
    }
}
