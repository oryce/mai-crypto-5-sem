package dora.messenger.client.ui.contact;

import dora.messenger.client.store.chat.ChatStore;
import dora.messenger.client.store.contact.Contact;
import dora.messenger.client.store.contact.ContactStore;
import dora.messenger.client.ui.chat.CreateChatDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.util.Objects.requireNonNull;

public class ContactCard extends JPanel {

    private final Contact contact;
    private final ContactStore contactStore;
    private final ChatStore chatStore;

    private final JButton deleteButton;

    public ContactCard(
        @NotNull Contact contact,
        @NotNull ContactStore contactStore,
        @NotNull ChatStore chatStore
    ) {
        this.contact = requireNonNull(contact, "contact");
        this.contactStore = requireNonNull(contactStore, "contact store");
        this.chatStore = requireNonNull(chatStore, "chat store");

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        JLabel nameLabel = new JLabel("%s %s".formatted(
            contact.otherUser().firstName(),
            contact.otherUser().lastName()
        ));
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(nameLabel, constraints);

        JLabel usernameLabel = new JLabel("@%s".formatted(contact.otherUser().username()));
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 1;
        add(usernameLabel, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridheight = 2;
        constraints.weightx = 0;

        JButton createChatButton = new JButton("Создать чат");
        createChatButton.addActionListener(new CreateChatActionListener());
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(0, 0, 0, 4);
        constraints.gridx = 1;
        constraints.gridy = 0;
        add(createChatButton, constraints);

        deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(new DeleteActionListener());
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.gridx = 2;
        constraints.gridy = 0;
        add(deleteButton, constraints);
    }

    private class DeleteActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            deleteButton.setEnabled(false);

            contactStore.deleteContact(contact)
                .whenComplete((nothing, throwable) -> {
                    if (throwable == null) return;

                    EventQueue.invokeLater(() -> {
                        deleteButton.setEnabled(true);

                        JOptionPane.showMessageDialog(
                            null,
                            "Не удалось удалить контакт",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                });
        }
    }

    private class CreateChatActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            var dialog = new CreateChatDialog(null, chatStore, contact.otherUser());
            dialog.setVisible(true);
        }
    }
}
