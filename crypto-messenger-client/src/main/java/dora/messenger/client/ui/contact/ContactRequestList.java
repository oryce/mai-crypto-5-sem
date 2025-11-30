package dora.messenger.client.ui.contact;

import dora.messenger.client.store.CollectionRef;
import dora.messenger.client.store.contact.ContactRequest;
import dora.messenger.client.store.contact.ContactRequestStore;
import org.jetbrains.annotations.NotNull;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.Collection;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ContactRequestList extends JPanel {

    private final ContactRequestStore requestStore;

    private final JTextField usernameField;
    private final JButton createRequestButton;
    private final JPanel requests;

    public ContactRequestList(@NotNull ContactRequestStore requestStore) {
        this.requestStore = requireNonNull(requestStore, "request store");

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.gridwidth = 2;

        JLabel requestsLabel = new JLabel("Запросы в контакты");
        requestsLabel.setFont(Font.getFont(Map.of(
            TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD,
            TextAttribute.SIZE, 16.0f
        )));
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(requestsLabel, constraints);

        JLabel createRequestLabel = new JLabel("Для отправки запроса в контакты введите имя пользователя:");
        constraints.insets = new Insets(8, 0, 0, 0);
        constraints.gridx = 0;
        constraints.gridy = 1;
        add(createRequestLabel, constraints);

        constraints.anchor = GridBagConstraints.NORTH;
        constraints.gridwidth = 1;

        usernameField = new JTextField();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(8, 0, 0, 0);
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 2;
        add(usernameField, constraints);

        createRequestButton = new JButton("Отправить запрос");
        createRequestButton.addActionListener(new CreateRequestActionListener());
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(8, 8, 0, 0);
        constraints.weightx = 0;
        constraints.gridx = 1;
        constraints.gridy = 2;
        add(createRequestButton, constraints);

        constraints.gridwidth = 2;

        JSeparator separator = new JSeparator();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(8, 0, 0, 0);
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 3;
        add(separator, constraints);

        requests = new JPanel();
        requests.setLayout(new BoxLayout(requests, BoxLayout.Y_AXIS));
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(8, 0, 0, 0);
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 4;
        add(requests, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 8, 0);
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 5;
        add(Box.createVerticalGlue(), constraints);

        CollectionRef<ContactRequest> observable = requestStore.getRequests();
        observable.observe(this::update);

        if (observable.get() == null)
            throw new IllegalStateException("Contact requests are not initialized");

        update(observable.get());
    }

    private void update(Collection<ContactRequest> newRequests) {
        requests.removeAll();

        newRequests.forEach((request) ->
            requests.add(new ContactRequestCard(request, requestStore))
        );

        requests.revalidate();
        requests.repaint();
    }

    private class CreateRequestActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();

            createRequestButton.setEnabled(false);
            usernameField.setText("");

            requestStore.createRequest(username)
                .whenComplete((nothing, throwable) ->
                    EventQueue.invokeLater(() -> createRequestButton.setEnabled(true))
                );
        }
    }
}
