package dora.messenger.client.store.contact;

import dora.messenger.client.api.ContactApi;
import dora.messenger.client.api.EventApi;
import dora.messenger.client.api.UserApi;
import dora.messenger.client.store.CollectionRef;
import dora.messenger.protocol.contact.ContactAddedEventDto;
import dora.messenger.protocol.contact.ContactRemovedEventDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Singleton
public class ContactStore {

    private final UserApi userApi;
    private final ContactApi contactApi;

    @Inject
    public ContactStore(
        @NotNull UserApi userApi,
        @NotNull ContactApi contactApi,
        @NotNull EventApi eventApi
    ) {
        this.userApi = requireNonNull(userApi, "user API");
        this.contactApi = requireNonNull(contactApi, "contact API");
        requireNonNull(eventApi, "event API");

        eventApi.subscribe(ContactAddedEventDto.class, (event) ->
            contactsRef.add(Contact.from(event.getContact()))
        );

        eventApi.subscribe(ContactRemovedEventDto.class, (event) ->
            contactsRef.removeIf((contact) -> contact.id().equals(event.getContactId()))
        );
    }

    private final CollectionRef<Contact> contactsRef = CollectionRef.ofNull();

    public CollectionRef<Contact> getContacts() {
        return contactsRef;
    }

    public CompletableFuture<Void> fetchContacts() {
        return userApi.getContacts()
            .thenAccept((contactDto) -> {
                Collection<Contact> contacts = contactDto.stream()
                    .map(Contact::from)
                    .collect(Collectors.toCollection(TreeSet::new));
                contactsRef.set(contacts);
            });
    }

    public CompletableFuture<Void> deleteContact(Contact contact) {
        return contactApi.delete(contact.id())
            .thenRun(() -> contactsRef.remove(contact));
    }
}
