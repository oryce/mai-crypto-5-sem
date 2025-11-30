package dora.messenger.client.ui.auth;

import dora.messenger.client.store.user.UserStore;
import dora.messenger.client.ui.router.Route;
import dora.messenger.client.ui.router.Router;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.Objects;

public class RegisterView extends JPanel {

    private final UserStore userStore;
    private final Router router;

    private final JTextField firstNameField;
    private final JTextField lastNameField;
    private final JTextField usernameField;
    private final JTextField passwordField;
    private final JButton registerButton;

    @Inject
    public RegisterView(@NotNull UserStore userStore, @NotNull Router router) {
        this.userStore = Objects.requireNonNull(userStore, "user store");
        this.router = Objects.requireNonNull(router, "router");

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

        JLabel firstNameLabel = new JLabel("Имя");
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridy = 1;
        constraints.insets = new Insets(16, 0, 0, 0);
        add(firstNameLabel, constraints);

        firstNameField = new JTextField(24);
        constraints.gridy = 2;
        constraints.insets = new Insets(4, 0, 0, 0);
        add(firstNameField, constraints);

        JLabel lastNameLabel = new JLabel("Фамилия");
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridy = 3;
        constraints.insets = new Insets(16, 0, 0, 0);
        add(lastNameLabel, constraints);

        lastNameField = new JTextField(24);
        constraints.gridy = 4;
        constraints.insets = new Insets(4, 0, 0, 0);
        add(lastNameField, constraints);

        JLabel usernameLabel = new JLabel("Имя пользователя");
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridy = 5;
        constraints.insets = new Insets(16, 0, 0, 0);
        add(usernameLabel, constraints);

        usernameField = new JTextField(24);
        constraints.gridy = 6;
        constraints.insets = new Insets(4, 0, 0, 0);
        add(usernameField, constraints);

        JLabel passwordLabel = new JLabel("Пароль");
        constraints.gridy = 7;
        constraints.insets = new Insets(8, 0, 0, 0);
        add(passwordLabel, constraints);

        passwordField = new JTextField(24);
        constraints.gridy = 8;
        constraints.insets = new Insets(4, 0, 0, 0);
        add(passwordField, constraints);

        registerButton = new JButton("Зарегистрироваться");
        registerButton.addActionListener(new RegisterActionListener());
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 9;
        constraints.insets = new Insets(16, 0, 0, 0);
        add(registerButton, constraints);

        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        loginPanel.add(new JLabel("Уже зарегистрированы?"));

        JLabel loginLabel = new JLabel("<html><a href=\"#\">Войти</a></html>");
        loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLabel.addMouseListener(new LoginClickListener());
        loginPanel.add(loginLabel);

        constraints.gridy = 10;
        constraints.insets = new Insets(16, 0, 0, 0);
        add(loginPanel, constraints);
    }

    private class RegisterActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            registerButton.setEnabled(false);

            userStore.register(
                firstNameField.getText(),
                lastNameField.getText(),
                usernameField.getText(),
                passwordField.getText()
            ).whenComplete((nothing, throwable) -> {
                if (throwable == null) {
                    EventQueue.invokeLater(this::registerSuccess);
                } else {
                    EventQueue.invokeLater(() -> registerFailed(throwable));
                }
            });
        }

        private void registerSuccess() {
            registerButton.setEnabled(true);
            router.navigate(Route.CONTACTS);
        }

        private void registerFailed(Throwable throwable) {
            registerButton.setEnabled(true);

            JOptionPane.showMessageDialog(
                null,
                throwable.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private class LoginClickListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            router.navigate(Route.LOGIN);
        }
    }
}
