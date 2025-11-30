package dora.messenger.client.store.contact;

import dora.messenger.client.store.user.User;
import dora.messenger.protocol.contact.ContactDto;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record Contact(
    @NotNull UUID id,
    @NotNull User otherUser
) implements Comparable<Contact> {

    public Contact {
        requireNonNull(id, "ID");
        requireNonNull(otherUser, "other user");
    }

    public static Contact from(@NotNull ContactDto contactDto) {
        requireNonNull(contactDto, "contact DTO");
        return new Contact(contactDto.id(), User.from(contactDto.user()));
    }

    @Override
    public int compareTo(@NotNull Contact other) {
        return Comparator.comparing((Contact contact) -> contact.otherUser().firstName())
            .thenComparing((contact) -> contact.otherUser().lastName())
            .thenComparing(Contact::id)
            .compare(this, other);
    }
}
