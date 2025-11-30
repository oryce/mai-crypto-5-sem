package dora.messenger.client;

import com.formdev.flatlaf.FlatLightLaf;
import dora.messenger.client.api.EventApi;
import dora.messenger.client.store.chat.ChatStore;
import dora.messenger.client.store.session.SessionCredentials;
import dora.messenger.client.store.session.SessionStore;
import dora.messenger.client.store.user.UserStore;
import dora.messenger.client.ui.MessengerFrame;
import dora.messenger.client.ui.router.Route;
import dora.messenger.client.ui.router.Router;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public class MessengerClientApplication {

    private static final Logger LOGGER = LogManager.getLogger(MessengerClientApplication.class);

    private final MessengerFrame frame;
    private final SessionStore sessionStore;
    private final UserStore userStore;
    private final ChatStore chatStore;
    private final Router router;
    private final EventApi eventApi;

    @Inject
    public MessengerClientApplication(
        @NotNull MessengerFrame frame,
        @NotNull SessionStore sessionStore,
        @NotNull UserStore userStore,
        @NotNull ChatStore chatStore,
        @NotNull Router router,
        @NotNull EventApi eventApi
    ) {
        this.frame = requireNonNull(frame, "messenger frame");
        this.sessionStore = requireNonNull(sessionStore, "session store");
        this.userStore = requireNonNull(userStore, "user store");
        this.chatStore = requireNonNull(chatStore, "chat store");
        this.router = requireNonNull(router, "router");
        this.eventApi = requireNonNull(eventApi, "event API");
    }

    public void run() {
        LOGGER.trace("Configuring LaF");
        FlatLightLaf.setup();

        sessionStore.getCredentials().observe((newCredentials) -> {
            if (newCredentials != null) {
                LOGGER.trace("Connecting to Event API (session created)");
                fetchInfo().thenRun(eventApi::connect);
            } else {
                LOGGER.trace("Disconnecting from Event API (session destroyed)");
                eventApi.disconnect();
            }
        });

        SessionCredentials credentials = sessionStore.getCredentials().get();

        if (credentials != null) {
            try {
                // TODO (17.12.25, ~oryce):
                //   Don't block here. Show a loading screen.
                LOGGER.trace("Database contains saved credentials, fetching user information");
                fetchInfo().get(5, TimeUnit.SECONDS);

                LOGGER.trace("Connecting to Event API (initial)");
                eventApi.connect();

                router.navigate(Route.CONTACTS);
            } catch (Exception e) {
                LOGGER.warn("Cannot fetch user information", e);
                router.navigate(Route.LOGIN);
            }
        } else {
            LOGGER.trace("Database doesn't contain saved credentials");
            router.navigate(Route.LOGIN);
        }

        frame.setVisible(true);
        LOGGER.info("Application started");
    }

    private CompletableFuture<Void> fetchInfo() {
        return userStore.fetch()
            // Chats must be loaded, otherwise the message listener doesn't know
            // about them and can't decrypt messages.
            .thenCompose((nothing) -> chatStore.fetchChats());
    }
}
