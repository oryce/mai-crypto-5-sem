package dora.messenger.client.store.contact;

import dora.messenger.client.api.ContactRequestApi;
import dora.messenger.client.api.EventApi;
import dora.messenger.client.api.UserApi;
import dora.messenger.client.store.CollectionRef;
import dora.messenger.protocol.contact.ContactRequestAddedEventDto;
import dora.messenger.protocol.contact.ContactRequestRemovedEventDto;
import dora.messenger.protocol.contact.CreateContactRequest;
import dora.messenger.protocol.contact.UpdateContactRequest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Singleton
public class ContactRequestStore {

    private final UserApi userApi;
    private final ContactRequestApi contactRequestApi;

    @Inject
    public ContactRequestStore(
        @NotNull UserApi userApi,
        @NotNull ContactRequestApi contactRequestApi,
        @NotNull EventApi eventApi
    ) {
        this.userApi = requireNonNull(userApi, "user API");
        this.contactRequestApi = requireNonNull(contactRequestApi, "contact request API");

        requireNonNull(eventApi, "event API");

        eventApi.subscribe(ContactRequestAddedEventDto.class, (event) ->
            contactRequests.add(ContactRequest.from(event.getRequest()))
        );

        eventApi.subscribe(ContactRequestRemovedEventDto.class, (event) ->
            contactRequests.removeIf((request) -> request.id().equals(event.getRequestId()))
        );
    }

    private final CollectionRef<ContactRequest> contactRequests = CollectionRef.ofNull();

    public CollectionRef<ContactRequest> getRequests() {
        return contactRequests;
    }

    public CompletableFuture<Void> fetchRequests() {
        return userApi.getContactRequests()
            .thenAccept((contactRequestDtos) -> {
                Collection<ContactRequest> requests = contactRequestDtos.stream()
                    .map(ContactRequest::from)
                    .collect(Collectors.toCollection(TreeSet::new));
                contactRequests.set(requests);
            });
    }

    public CompletableFuture<Void> createRequest(@NotNull String username) {
        requireNonNull(username, "username");

        return contactRequestApi.create(new CreateContactRequest(username))
            .thenAccept((contactRequest) -> contactRequests.add(ContactRequest.from(contactRequest)));
    }

    public CompletableFuture<Void> cancelRequest(@NotNull ContactRequest request) {
        requireNonNull(request, "request");

        return contactRequestApi.cancel(request.id())
            .thenRun(() -> contactRequests.remove(request));
    }

    public CompletableFuture<Void> acceptRequest(@NotNull ContactRequest request) {
        requireNonNull(request, "request");

        return contactRequestApi.update(request.id(), new UpdateContactRequest(true))
            .thenRun(() -> contactRequests.remove(request));
    }

    public CompletableFuture<Void> rejectRequest(@NotNull ContactRequest request) {
        requireNonNull(request, "request");

        return contactRequestApi.update(request.id(), new UpdateContactRequest(false))
            .thenRun(() -> contactRequests.remove(request));
    }
}
