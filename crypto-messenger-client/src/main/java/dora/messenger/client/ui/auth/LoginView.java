package dora.messenger.client.ui.auth;

import dora.messenger.client.store.session.SessionStore;
import dora.messenger.client.ui.router.Route;
import dora.messenger.client.ui.router.Router;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class LoginView extends JPanel {

    private static final Logger LOGGER = LogManager.getLogger(LoginView.class);

    private final SessionStore sessionStore;
    private final Router router;

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;

    @Inject
    public LoginView(@NotNull SessionStore sessionStore, @NotNull Router router) {
        this.sessionStore = requireNonNull(sessionStore, "session store");
        this.router = requireNonNull(router, "router");

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        JLabel titleLabel = new JLabel("Dora Messenger");
        titleLabel.setFont(Font.getFont(Map.of(
            TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD,
            TextAttribute.SIZE, 20.0f
        )));
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        add(titleLabel, constraints);

        JLabel usernameLabel = new JLabel("Имя пользователя");
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridy = 1;
        constraints.insets = new Insets(16, 0, 0, 0);
        add(usernameLabel, constraints);

        usernameField = new JTextField(24);
        constraints.gridy = 2;
        constraints.insets = new Insets(4, 0, 0, 0);
        add(usernameField, constraints);

        JLabel passwordLabel = new JLabel("Пароль");
        constraints.gridy = 3;
        constraints.insets = new Insets(8, 0, 0, 0);
        add(passwordLabel, constraints);

        passwordField = new JPasswordField(24);
        constraints.gridy = 4;
        constraints.insets = new Insets(4, 0, 0, 0);
        add(passwordField, constraints);

        loginButton = new JButton("Войти");
        loginButton.addActionListener(new LoginActionListener());
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 5;
        constraints.insets = new Insets(16, 0, 0, 0);
        add(loginButton, constraints);

        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        registerPanel.add(new JLabel("Нет аккаунта?"));

        JLabel registerLabel = new JLabel("<html><a href=\"#\">Зарегистрироваться</a></html>");
        registerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLabel.addMouseListener(new RegisterClickListener());
        registerPanel.add(registerLabel);

        constraints.gridy = 6;
        constraints.insets = new Insets(16, 0, 0, 0);
        add(registerPanel, constraints);
    }

    private class LoginActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            loginButton.setEnabled(false);

            sessionStore.login(usernameField.getText(), passwordField.getText())
                .whenComplete((nothing, throwable) -> {
                    if (throwable == null) {
                        EventQueue.invokeLater(this::loginSuccess);
                    } else {
                        EventQueue.invokeLater(() -> loginFailure(throwable.getCause()));
                    }
                });
        }

        private void loginSuccess() {
            loginButton.setEnabled(true);
            router.navigate(Route.CONTACTS);
        }

        private void loginFailure(Throwable throwable) {
            loginButton.setEnabled(true);

            LOGGER.error("Cannot sign in", throwable);

            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(LoginView.this),
                "Произошла ошибка при входе. Проверьте данные и повторите попытку",
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private class RegisterClickListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            router.navigate(Route.REGISTER);
        }
    }
}
