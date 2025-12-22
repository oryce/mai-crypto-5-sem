package dora.messenger.client.ui.chat;

import dora.messenger.client.persistence.ChatSession;
import dora.messenger.client.store.chat.Chat;
import dora.messenger.client.store.chat.ChatSessionStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.util.Objects.requireNonNull;

public class ChatHeader extends JPanel {

    private static final Logger LOGGER = LogManager.getLogger(ChatHeader.class);

    private final ChatSession chatSession;
    private final ChatSessionStore chatSessionStore;

    public ChatHeader(
        @NotNull Chat chat,
        @NotNull ChatSession chatSession,
        @NotNull ChatSessionStore chatSessionStore
    ) {
        requireNonNull(chat, "chat");
        this.chatSession = requireNonNull(chatSession, "chat session");
        this.chatSessionStore = requireNonNull(chatSessionStore, "chat session store");

        setLayout(new BorderLayout());

        JLabel chatNameLabel = new JLabel(chat.name());
        chatNameLabel.setFont(chatNameLabel.getFont().deriveFont(Font.BOLD, 18f));
        add(chatNameLabel, BorderLayout.WEST);

        JButton disconnectButton = new JButton("Отключиться");
        disconnectButton.addActionListener(new DisconnectActionListener());
        add(disconnectButton, BorderLayout.EAST);
    }

    private class DisconnectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            chatSessionStore.deleteSession(chatSession).whenComplete((nothing, throwable) -> {
                if (throwable != null) SwingUtilities.invokeLater(() -> disconnectFailed(throwable));
            });
        }

        private void disconnectFailed(Throwable throwable) {
            LOGGER.error("Cannot delete session", throwable);

            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(ChatHeader.this),
                "Произошла ошибка при завершении соединения",
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
