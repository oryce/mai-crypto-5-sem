package dora.messenger.client.ui.chat;

import dora.messenger.client.store.chat.Chat;
import dora.messenger.client.store.chat.ChatSessionStore;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.util.Objects.requireNonNull;

public class ChatConnectView extends JPanel {

    private final Chat chat;
    private final ChatSessionStore chatSessionStore;

    public ChatConnectView(@NotNull Chat chat, @NotNull ChatSessionStore chatSessionStore) {
        this.chat = requireNonNull(chat, "chat");
        this.chatSessionStore = requireNonNull(chatSessionStore, "chat session store");

        setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridBagLayout());
        add(content, BorderLayout.CENTER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("Подключение к чату");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 8, 0);
        content.add(titleLabel, constraints);

        JLabel descriptionLabel = new JLabel("Для подключения нажмите на кнопку ниже");
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 0, 12, 0);
        content.add(descriptionLabel, constraints);

        JButton connectButton = new JButton("Подключиться");
        connectButton.addActionListener(new ConnectActionListener());
        constraints.gridy = 2;
        constraints.insets = new Insets(0, 0, 0, 0);
        content.add(connectButton, constraints);
    }

    private class ConnectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            chatSessionStore.createSession(chat).whenComplete((nothing, throwable) -> {
                if (throwable != null) EventQueue.invokeLater(() -> connectFailed(throwable));
            });
        }

        private void connectFailed(Throwable throwable) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(ChatConnectView.this),
                throwable.getMessage(),
                "Ошибка создания соединения",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
