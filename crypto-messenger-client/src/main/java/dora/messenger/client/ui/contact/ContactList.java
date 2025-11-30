package dora.messenger.client.ui.contact;

import dora.messenger.client.store.CollectionRef;
import dora.messenger.client.store.chat.ChatStore;
import dora.messenger.client.store.contact.Contact;
import dora.messenger.client.store.contact.ContactStore;
import dora.messenger.client.store.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ContactList extends JPanel {

    private final ContactStore contactStore;
    private final ChatStore chatStore;

    private final JPanel contacts;

    public ContactList(
        @NotNull ContactStore contactStore,
        @NotNull ChatStore chatStore
    ) {
        this.contactStore = requireNonNull(contactStore, "contact store");
        this.chatStore = requireNonNull(chatStore, "chat store");

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        JLabel requestsLabel = new JLabel("Контакты");
        requestsLabel.setFont(Font.getFont(Map.of(
            TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD,
            TextAttribute.SIZE, 16.0f
        )));
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.gridy = 0;
        add(requestsLabel, constraints);

        contacts = new JPanel();
        contacts.setLayout(new BoxLayout(contacts, BoxLayout.Y_AXIS));
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(8, 0, 0, 0);
        constraints.weightx = 1;
        constraints.gridy = 1;
        add(contacts, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 8, 0);
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridy = 2;
        add(Box.createVerticalGlue(), constraints);

        CollectionRef<Contact> observable = contactStore.getContacts();
        observable.observe(this::update);

        if (observable.get() == null)
            throw new IllegalStateException("Contacts are not initialized");

        update(observable.get());
    }

    private void update(Collection<Contact> contactList) {
        contacts.removeAll();

        contactList.forEach((request) ->
            contacts.add(new ContactCard(request, contactStore, chatStore))
        );

        contacts.revalidate();
        contacts.repaint();
    }
}
