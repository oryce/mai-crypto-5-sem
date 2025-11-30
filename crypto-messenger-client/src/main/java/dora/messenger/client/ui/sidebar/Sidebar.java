package dora.messenger.client.ui.sidebar;

import dora.messenger.client.store.chat.ChatStore;
import dora.messenger.client.store.session.SessionStore;
import dora.messenger.client.store.user.UserStore;
import dora.messenger.client.ui.router.Route;
import dora.messenger.client.ui.router.Router;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import static java.util.Objects.requireNonNull;

@Singleton
public class Sidebar extends JPanel {

    @Inject
    public Sidebar(
        @NotNull SessionStore sessionStore,
        @NotNull UserStore userStore,
        @NotNull ChatStore chatStore,
        @NotNull Router router
    ) {
        requireNonNull(sessionStore, "session store");
        requireNonNull(userStore, "user store");
        requireNonNull(chatStore, "chat store");
        requireNonNull(router, "router");

        setLayout(new GridBagLayout());
        setMinimumSize(new Dimension(160, 0));

        GridBagConstraints constraints = new GridBagConstraints();

        JButton contactsButton = new JButton("Контакты");
        contactsButton.addActionListener((event) -> router.navigate(Route.CONTACTS));
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 0, 10);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridy = 0;
        add(contactsButton, constraints);

        ChatList chatList = new ChatList(chatStore, router);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(10, 10, 0, 10);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridy = 1;
        add(chatList, constraints);

        JSeparator separator = new JSeparator();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 0, 10);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridy = 2;
        add(separator, constraints);

        UserCard userCard = new UserCard(userStore, sessionStore, router);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridy = 3;
        add(userCard, constraints);
    }
}
