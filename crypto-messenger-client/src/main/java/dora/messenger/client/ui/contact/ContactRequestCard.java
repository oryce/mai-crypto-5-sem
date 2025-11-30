package dora.messenger.client.ui.contact;

import dora.messenger.client.store.contact.ContactRequest;
import dora.messenger.client.store.contact.ContactRequest.Direction;
import dora.messenger.client.store.contact.ContactRequestStore;
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

public class ContactRequestCard extends JPanel {

    private final ContactRequest request;
    private final ContactRequestStore requestStore;

    private JButton acceptButton;
    private JButton rejectButton;
    private JButton cancelButton;

    public ContactRequestCard(
        @NotNull ContactRequest request,
        @NotNull ContactRequestStore requestStore
    ) {
        this.request = requireNonNull(request, "request");
        this.requestStore = requireNonNull(requestStore, "request store");

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        JLabel nameLabel = new JLabel("%s %s".formatted(
            request.user().firstName(),
            request.user().lastName()
        ));
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(nameLabel, constraints);

        JLabel directionLabel = new JLabel(switch (request.direction()) {
            case INCOMING -> "Входящий запрос";
            case OUTGOING -> "Исходящий запрос";
        });
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 1;
        add(directionLabel, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridheight = 2;
        constraints.weightx = 0;

        if (request.direction() == Direction.INCOMING) {
            acceptButton = new JButton("Принять");
            acceptButton.addActionListener(new AcceptActionListener());
            constraints.anchor = GridBagConstraints.EAST;
            constraints.insets = new Insets(0, 0, 0, 4);
            constraints.gridx = 1;
            constraints.gridy = 0;
            add(acceptButton, constraints);

            rejectButton = new JButton("Отклонить");
            rejectButton.addActionListener(new RejectActionListener());
            constraints.anchor = GridBagConstraints.EAST;
            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.gridx = 2;
            constraints.gridy = 0;
            add(rejectButton, constraints);
        } else {
            cancelButton = new JButton("Отменить");
            cancelButton.addActionListener(new CancelActionListener());
            constraints.anchor = GridBagConstraints.EAST;
            constraints.gridx = 1;
            constraints.gridy = 0;
            add(cancelButton, constraints);
        }
    }

    private class AcceptActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            acceptButton.setEnabled(false);

            requestStore.acceptRequest(request)
                .whenComplete((nothing, throwable) -> {
                    if (throwable == null) return;

                    EventQueue.invokeLater(() -> {
                        acceptButton.setEnabled(true);

                        JOptionPane.showMessageDialog(
                            null,
                            throwable.getMessage(),
                            "Ошибка принятия запроса в контакты",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                });
        }
    }

    private class RejectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            rejectButton.setEnabled(false);

            requestStore.rejectRequest(request)
                .whenComplete((nothing, throwable) -> {
                    if (throwable == null) return;

                    EventQueue.invokeLater(() -> {
                        rejectButton.setEnabled(true);

                        JOptionPane.showMessageDialog(
                            null,
                            "Не удалось отклонить запрос в контакты",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                });
        }
    }

    private class CancelActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            cancelButton.setEnabled(false);

            requestStore.cancelRequest(request)
                .whenComplete((nothing, throwable) -> {
                    if (throwable == null) return;

                    EventQueue.invokeLater(() -> {
                        cancelButton.setEnabled(true);

                        JOptionPane.showMessageDialog(
                            null,
                            "Не удалось отменить запрос в контакты",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                });
        }
    }
}
