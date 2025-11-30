package dora.messenger.client.store.user;

import dora.messenger.client.api.UserApi;
import dora.messenger.client.store.Observable;
import dora.messenger.client.store.Ref;
import dora.messenger.client.store.session.SessionCredentials;
import dora.messenger.client.store.session.SessionStore;
import dora.messenger.protocol.user.CreateUser;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

@Singleton
public class UserStore {

    private final UserApi userApi;
    private final SessionStore sessionStore;

    @Inject
    public UserStore(
        @NotNull UserApi userApi,
        @NotNull SessionStore sessionStore
    ) {
        this.userApi = requireNonNull(userApi, "user API");
        this.sessionStore = requireNonNull(sessionStore, "session store");
    }

    private final Ref<User> userRef = Ref.ofNull();

    public Ref<User> get() {
        return userRef;
    }

    public CompletableFuture<Void> fetch() {
        return userApi.getUser().thenAccept((userDto) -> userRef.set(User.from(userDto)));
    }

    public CompletableFuture<Void> register(
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull String username,
        @NotNull String password
    ) {
        requireNonNull(firstName, "first name");
        requireNonNull(lastName, "last name");
        requireNonNull(username, "username");
        requireNonNull(password, "password");

        return userApi.createUser(new CreateUser(firstName, lastName, username, password))
            .thenAccept((createdUser) -> {
                sessionStore.getCredentials().set(SessionCredentials.from(createdUser.credentials()));
                userRef.set(User.from(createdUser.user()));
            });
    }
}
