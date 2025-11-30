package dora.messenger.client.ui.chat;

import dora.messenger.client.persistence.ChatSession;
import dora.messenger.client.store.chat.Chat;
import dora.messenger.client.store.chat.ChatFileStore;
import dora.messenger.client.store.chat.ChatMessageStore;
import dora.messenger.client.store.chat.ChatSessionStore;
import dora.messenger.client.store.user.UserStore;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JSeparator;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import static java.util.Objects.requireNonNull;

public class ChatMessagesView extends JPanel {

    public ChatMessagesView(
        @NotNull Chat chat,
        @NotNull ChatSession chatSession,
        @NotNull ChatSessionStore chatSessionStore,
        @NotNull ChatMessageStore chatMessageStore,
        @NotNull ChatFileStore chatFileStore,
        @NotNull UserStore userStore
    ) {
        requireNonNull(chat, "chat");
        requireNonNull(chatSession, "chat session");
        requireNonNull(chatSessionStore, "chat session store");

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 0, 10);

        ChatHeader header = new ChatHeader(chat, chatSession, chatSessionStore);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridy = 0;
        add(header, constraints);

        JSeparator separator = new JSeparator();
        constraints.gridy = 1;
        constraints.insets = new Insets(8, 10, 0, 10);
        add(separator, constraints);

        ChatMessageList messageList = new ChatMessageList(
            chat,
            chatSession,
            chatFileStore,
            chatMessageStore,
            userStore
        );
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(8, 10, 0, 10);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridy = 2;
        add(messageList, constraints);

        ChatMessageInput messageInput = new ChatMessageInput(
            chat,
            chatSession,
            chatMessageStore,
            chatFileStore
        );
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridy = 3;
        add(messageInput, constraints);
    }
}
