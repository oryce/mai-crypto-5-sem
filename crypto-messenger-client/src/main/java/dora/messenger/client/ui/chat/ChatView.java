package dora.messenger.client.ui.chat;

import com.google.inject.assistedinject.Assisted;
import dora.messenger.client.persistence.ChatSession;
import dora.messenger.client.store.Computed;
import dora.messenger.client.store.chat.Chat;
import dora.messenger.client.store.chat.ChatFileStore;
import dora.messenger.client.store.chat.ChatMessageStore;
import dora.messenger.client.store.chat.ChatSessionStore;
import dora.messenger.client.store.user.UserStore;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import java.awt.BorderLayout;

import static java.util.Objects.requireNonNull;

public class ChatView extends JPanel {

    private final ChatSessionStore chatSessionStore;
    private final ChatMessageStore chatMessageStore;
    private final ChatFileStore chatFileStore;
    private final UserStore userStore;
    private final Chat chat;

    @Inject
    public ChatView(
        @NotNull ChatSessionStore chatSessionStore,
        @NotNull ChatMessageStore chatMessageStore,
        @NotNull ChatFileStore chatFileStore,
        @NotNull UserStore userStore,
        @Assisted @NotNull Chat chat
    ) {
        this.chatSessionStore = requireNonNull(chatSessionStore, "session store");
        this.chatMessageStore = requireNonNull(chatMessageStore, "message store");
        this.chatFileStore = requireNonNull(chatFileStore, "file store");
        this.userStore = requireNonNull(userStore, "user store");
        this.chat = requireNonNull(chat, "chat");

        setLayout(new BorderLayout());

        Computed<ChatSession> chatSession = chatSessionStore.getSession(chat);
        chatSession.observe(this::chatSessionUpdated);
        chatSessionUpdated(chatSession.get());
    }

    private void chatSessionUpdated(ChatSession chatSession) {
        removeAll();

        if (chatSession == null) {
            ChatConnectView connectView = new ChatConnectView(chat, chatSessionStore);
            add(connectView, BorderLayout.CENTER);
        } else if (!chatSession.isComplete()) {
            ChatConnectingView connectingView = new ChatConnectingView(chatSession, chatSessionStore);
            add(connectingView, BorderLayout.CENTER);
        } else {
            ChatMessagesView messagesView = new ChatMessagesView(
                chat,
                chatSession,
                chatSessionStore,
                chatMessageStore,
                chatFileStore,
                userStore
            );
            add(messagesView, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }
}
