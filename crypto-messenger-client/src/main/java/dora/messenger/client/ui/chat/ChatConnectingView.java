package dora.messenger.client.ui.chat;

import dora.messenger.client.persistence.ChatSession;
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

public class ChatConnectingView extends JPanel {

    private final ChatSession chatSession;
    private final ChatSessionStore chatSessionStore;

    public ChatConnectingView(@NotNull ChatSession session, @NotNull ChatSessionStore chatSessionStore) {
        this.chatSession = requireNonNull(session, "chat session");
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

        JLabel descriptionLabel = new JLabel("Ожидание ответа другого пользователя...");
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 0, 12, 0);
        content.add(descriptionLabel, constraints);

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(new CancelActionListener());
        constraints.gridy = 2;
        constraints.insets = new Insets(0, 0, 0, 0);
        content.add(cancelButton, constraints);
    }

    private class CancelActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            chatSessionStore.deleteSession(chatSession).whenComplete((nothing, throwable) -> {
                if (throwable != null) EventQueue.invokeLater(() -> cancelFailed(throwable));
            });
        }

        private void cancelFailed(Throwable throwable) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(ChatConnectingView.this),
                throwable.getMessage(),
                "Ошибка отмены соединения",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
