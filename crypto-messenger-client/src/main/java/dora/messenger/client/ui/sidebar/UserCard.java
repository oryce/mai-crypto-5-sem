package dora.messenger.client.ui.sidebar;

import dora.messenger.client.store.Ref;
import dora.messenger.client.store.session.SessionStore;
import dora.messenger.client.store.user.User;
import dora.messenger.client.store.user.UserStore;
import dora.messenger.client.ui.router.Route;
import dora.messenger.client.ui.router.Router;
import org.jetbrains.annotations.NotNull;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.util.Objects.requireNonNull;

public class UserCard extends JPanel {

    private final SessionStore sessionStore;
    private final Router router;

    private final JLabel nameLabel;
    private final JLabel tagLabel;
    private final JPopupMenu popupMenu;

    public UserCard(
        @NotNull UserStore userStore,
        @NotNull SessionStore sessionStore,
        @NotNull Router router
    ) {
        requireNonNull(userStore, "user store");
        this.sessionStore = requireNonNull(sessionStore, "session store");
        this.router = requireNonNull(router, "router");

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1;
        constraints.weighty = 1;

        nameLabel = new JLabel("Загрузка...");
        constraints.gridy = 0;
        add(nameLabel, constraints);

        tagLabel = new JLabel();
        constraints.gridy = 1;
        add(tagLabel, constraints);

        popupMenu = new JPopupMenu();

        JMenuItem logoutItem = new JMenuItem("Выход");
        logoutItem.addActionListener(new LogoutListener());
        popupMenu.add(logoutItem);

        addMouseListener(new MenuClickListener());

        Ref<User> user = userStore.get();
        user.observe(this::update);

        if (user.get() != null) {
            // User fetched before we mounted.
            update(user.get());
        }
    }

    private void update(User user) {
        nameLabel.setText("%s %s".formatted(user.firstName(), user.lastName()));
        tagLabel.setText("@%s".formatted(user.username()));
    }

    private class MenuClickListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                popupMenu.show(UserCard.this, e.getX(), e.getY());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) showMenu(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) showMenu(e);
        }

        private void showMenu(MouseEvent e) {
            popupMenu.show(UserCard.this, e.getX(), e.getY());
        }
    }

    private class LogoutListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            sessionStore.logout()
                .whenComplete((nothing, throwable) -> {
                    if (throwable == null) {
                        EventQueue.invokeLater(() -> router.navigate(Route.LOGIN));
                    } else {
                        EventQueue.invokeLater(() -> logoutFailed(throwable.getCause()));
                    }
                });
        }

        private void logoutFailed(Throwable throwable) {
            JOptionPane.showMessageDialog(
                null,
                throwable.getMessage(),
                "Ошибка выхода",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
