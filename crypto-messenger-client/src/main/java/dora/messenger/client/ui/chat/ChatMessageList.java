package dora.messenger.client.ui.chat;

import dora.messenger.client.persistence.ChatMessage;
import dora.messenger.client.persistence.ChatSession;
import dora.messenger.client.store.ObservableCollection.CollectionObserver;
import dora.messenger.client.store.Ref;
import dora.messenger.client.store.chat.Chat;
import dora.messenger.client.store.chat.ChatFileStore;
import dora.messenger.client.store.chat.ChatMessageStore;
import dora.messenger.client.store.chat.ChatMessageStore.MessageCollectionRef;
import dora.messenger.client.store.user.User;
import dora.messenger.client.store.user.UserStore;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class ChatMessageList extends JPanel {

    private final Chat chat;
    private final ChatSession session;
    private final ChatFileStore fileStore;

    private final JPanel messages;
    private final Component spacer;

    private int row = 0;

    public ChatMessageList(
        @NotNull Chat chat,
        @NotNull ChatSession session,
        @NotNull ChatFileStore fileStore,
        @NotNull ChatMessageStore messageStore,
        @NotNull UserStore userStore
    ) {
        this.chat = requireNonNull(chat, "chat");
        this.session = requireNonNull(session, "chat session");
        this.fileStore = requireNonNull(fileStore, "file store");

        setLayout(new BorderLayout());

        messages = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(messages);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                for (Component component : messages.getComponents()) {
                    if (component instanceof ChatMessageBubble message) {
                        message.updateWidth();
                    }
                }
            }
        });
        add(scrollPane, BorderLayout.CENTER);

        spacer = Box.createVerticalGlue();
        addSpacer();

        MessageCollectionRef messagesRef = messageStore.getMessages(chat);
        Ref<User> userRef = userStore.get();

        messagesRef.observe(new CollectionObserver<>() {

            @Override
            public void itemAdded(ChatMessage message) {
                addMessage(message, userRef.get());
            }

            @Override
            public void itemRemoved(ChatMessage item) {
            }

            @Override
            public void valueChanged(Collection<ChatMessage> newValue) {
            }
        });

        messagesRef.get().forEach((message) -> addMessage(message, userRef.get()));
    }

    public void addMessage(ChatMessage message, User currentUser) {
        messages.remove(spacer);
        row--;

        var messageBubble = new ChatMessageBubble(chat, session, fileStore, message, currentUser);
        addMessageBubble(messageBubble);
        addSpacer();

        messages.revalidate();
        messages.repaint();

        SwingUtilities.invokeLater(() -> {
            var bubbleBounds = messageBubble.getBounds();
            messageBubble.scrollRectToVisible(bubbleBounds);
        });
    }

    private void addMessageBubble(ChatMessageBubble message) {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.anchor = message.byCurrentUser()
            ? GridBagConstraints.EAST
            : GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 10, 0);
        constraints.weightx = 1;
        constraints.gridy = row++;
        messages.add(message, constraints);
    }

    private void addSpacer() {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridy = row++;
        messages.add(spacer, constraints);
    }
}
