package dora.messenger.client.ui.router;

import dora.messenger.client.store.Observable;
import dora.messenger.client.store.Ref;
import dora.messenger.client.ui.MessengerFrame;
import dora.messenger.client.ui.auth.LoginView;
import dora.messenger.client.ui.auth.RegisterView;
import dora.messenger.client.ui.contact.ContactView;
import dora.messenger.client.ui.sidebar.Sidebar;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

@Singleton
public class Router {

    private final MessengerFrame frame;
    private final Provider<LoginView> loginView;
    private final Provider<RegisterView> registerView;
    private final Provider<Sidebar> sidebar;
    private final Provider<ContactView> contactView;
    private final ViewFactory viewFactory;

    private final Ref<Route> currentRoute = Ref.ofNull();

    @Inject
    public Router(
        @NotNull MessengerFrame frame,
        @NotNull Provider<LoginView> loginView,
        @NotNull Provider<RegisterView> registerView,
        @NotNull Provider<Sidebar> sidebar,
        @NotNull Provider<ContactView> contactView,
        @NotNull ViewFactory viewFactory
    ) {
        this.frame = requireNonNull(frame, "frame");
        this.loginView = requireNonNull(loginView, "login view provider");
        this.registerView = requireNonNull(registerView, "register view provider");
        this.sidebar = requireNonNull(sidebar, "sidebar provider");
        this.contactView = requireNonNull(contactView, "contact view provider");
        this.viewFactory = requireNonNull(viewFactory, "view factory");
    }

    public void navigate(Route route) {
        frame.setContentPane(route.contentPane(this));
        frame.revalidate();
        frame.repaint();

        currentRoute.set(route);
    }

    public Ref<Route> currentRoute() {
        return currentRoute;
    }

    //region Accessors (package-private for `Route`)
    ViewFactory viewFactory() {
        return viewFactory;
    }

    LoginView loginView() {
        return loginView.get();
    }

    RegisterView registerView() {
        return registerView.get();
    }

    Sidebar sidebar() {
        return sidebar.get();
    }

    ContactView contactView() {
        return contactView.get();
    }
    //endregion
}
