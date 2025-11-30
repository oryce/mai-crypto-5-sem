package dora.messenger.client.store.contact;

import dora.messenger.client.store.user.User;
import dora.messenger.protocol.contact.ContactRequestDto;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * @param id        request ID
 * @param direction request direction
 * @param user      initiating or responding user
 */
public record ContactRequest(
    @NotNull UUID id,
    @NotNull Direction direction,
    @NotNull User user
) implements Comparable<ContactRequest> {

    public ContactRequest {
        requireNonNull(id, "request ID");
        requireNonNull(direction, "request direction");
        requireNonNull(user, "user");
    }

    public static ContactRequest from(@NotNull ContactRequestDto requestDto) {
        requireNonNull(requestDto, "request DTO");

        return new ContactRequest(
            requestDto.id(),
            Direction.from(requestDto.direction()),
            User.from(requestDto.user())
        );
    }

    @Override
    public int compareTo(@NotNull ContactRequest other) {
        return Comparator.comparing(ContactRequest::direction)
            .thenComparing(ContactRequest::id)
            .compare(this, other);
    }

    public enum Direction {

        INCOMING,
        OUTGOING;

        public static Direction from(@NotNull ContactRequestDto.Direction directionDto) {
            requireNonNull(directionDto, "direction DTO");

            return switch (directionDto) {
                case INCOMING -> Direction.INCOMING;
                case OUTGOING -> Direction.OUTGOING;
            };
        }
    }
}
