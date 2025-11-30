package dora.messenger.client.ui.contact;

import dora.messenger.client.store.chat.ChatStore;
import dora.messenger.client.store.contact.ContactRequestStore;
import dora.messenger.client.store.contact.ContactStore;
import dora.messenger.client.ui.LoadingView;
import dora.messenger.client.ui.MountListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.util.Objects;

@Singleton
public class ContactView extends JPanel {

    private final ContactStore contactStore;
    private final ChatStore chatStore;
    private final ContactRequestStore requestStore;

    private final CardLayout layout;
    private final JTabbedPane contactsPanel;

    @Inject
    public ContactView(
        @NotNull ContactStore contactStore,
        @NotNull ChatStore chatStore,
        @NotNull ContactRequestStore requestStore
    ) {
        this.contactStore = Objects.requireNonNull(contactStore, "contact store");
        this.chatStore = Objects.requireNonNull(chatStore, "chat store");
        this.requestStore = Objects.requireNonNull(requestStore, "request store");

        layout = new CardLayout();
        setLayout(layout);

        LoadingView loadingPanel = new LoadingView();
        add(loadingPanel, "loading");

        contactsPanel = new JTabbedPane();
        add(contactsPanel, "contacts");

        addAncestorListener(new ContactViewMountListener());
    }

    public void showLoading() {
        layout.show(this, "loading");
        revalidate();
        repaint();
    }

    public void showContacts() {
        contactsPanel.removeAll();

        contactsPanel.addTab("Контакты", createTab(new ContactList(contactStore, chatStore)));
        contactsPanel.addTab("Запросы", createTab(new ContactRequestList(requestStore)));
        contactsPanel.setSelectedIndex(0);

        layout.show(this, "contacts");
        revalidate();
        repaint();
    }

    private JScrollPane createTab(JPanel wrapped) {
        wrapped.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));

        JScrollPane scrollPane = new JScrollPane(
            wrapped,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 4));

        return scrollPane;
    }

    private class ContactViewMountListener extends MountListener {

        @Override
        public void componentMounted() {
            showLoading();

            contactStore.fetchContacts()
                .thenCompose((nothing) -> requestStore.fetchRequests())
                .whenComplete((nothing, throwable) -> {
                    if (throwable == null) {
                        EventQueue.invokeLater(ContactView.this::showContacts);
                    } else {
                        // TODO (16.12.25, ~oryce):
                        //   Handle errors.
                    }
                });
        }
    }
}
