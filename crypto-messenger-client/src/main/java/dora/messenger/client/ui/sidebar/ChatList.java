package dora.messenger.client.ui.sidebar;

import dora.messenger.client.store.CollectionRef;
import dora.messenger.client.store.Ref;
import dora.messenger.client.store.chat.Chat;
import dora.messenger.client.store.chat.ChatStore;
import dora.messenger.client.ui.router.ChatRoute;
import dora.messenger.client.ui.router.Route;
import dora.messenger.client.ui.router.Router;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class ChatList extends JPanel {

    private final ChatStore chatStore;
    private final Router router;
    private final JPanel cardsContainer;

    public ChatList(@NotNull ChatStore chatStore, @NotNull Router router) {
        this.chatStore = requireNonNull(chatStore, "chat store");
        this.router = requireNonNull(router, "router");

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Чаты"));

        cardsContainer = new JPanel(new GridBagLayout());
        cardsContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JScrollPane scrollPane = new JScrollPane(
            cardsContainer,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        CollectionRef<Chat> chats = chatStore.getChats();
        Ref<Route> route = router.currentRoute();

        chats.observe((newChats) -> chatsUpdated(newChats, route.get()));
        route.observe((newRoute) -> chatsUpdated(chats.get(), newRoute));
    }

    private void chatsUpdated(Collection<Chat> chats, Route route) {
        if (chats == null) return;
        Chat activeChat = route instanceof ChatRoute(Chat chat) ? chat : null;
        EventQueue.invokeLater(() -> renderChats(chats, activeChat));
    }

    private void renderChats(Collection<Chat> chats, @Nullable Chat activeChat) {
        cardsContainer.removeAll();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(0, 0, 6, 0);

        if (chats.isEmpty()) {
            JLabel emptyLabel = new JLabel("Чатов пока нет");
            cardsContainer.add(emptyLabel, constraints);
        } else {
            for (Chat chat : chats) {
                boolean active = activeChat != null && chat.id().equals(activeChat.id());

                ChatCard card = new ChatCard(
                    chat,
                    active,
                    () -> selectChat(chat),
                    () -> deleteChat(chat)
                );

                cardsContainer.add(card, constraints);
                constraints.gridy++;
            }
        }

        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        cardsContainer.add(Box.createVerticalGlue(), constraints);

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private void selectChat(Chat chat) {
        router.navigate(new ChatRoute(chat));
    }

    private void deleteChat(Chat chat) {
        chatStore.deleteChat(chat).whenComplete((nothing, throwable) -> {
            if (throwable == null) return;

            EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                throwable.getMessage(),
                "Ошибка удаления чата",
                JOptionPane.ERROR_MESSAGE
            ));
        });
    }
}
