package dora.messenger.client.ui.chat;

import dora.crypto.SymmetricCipher.CipherModeType;
import dora.crypto.SymmetricCipher.PaddingType;
import dora.messenger.client.store.chat.Chat.Algorithm;
import dora.messenger.client.store.chat.Chat.DiffieHellmanGroupId;
import dora.messenger.client.store.chat.ChatStore;
import dora.messenger.client.store.user.User;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.util.Objects.requireNonNull;

public class CreateChatDialog extends JDialog {

    private final ChatStore chatStore;
    private final User participant;

    private final JComboBox<DiffieHellmanGroupId> dhGroupCombo;
    private final JComboBox<Algorithm> algorithmCombo;
    private final JComboBox<CipherModeType> cipherModeCombo;
    private final JComboBox<PaddingType> paddingCombo;
    private final JButton createButton;

    public CreateChatDialog(
        JFrame parent,
        @NotNull ChatStore chatStore,
        @NotNull User participant
    ) {
        super(parent, true);

        this.chatStore = requireNonNull(chatStore, "chat store");
        this.participant = requireNonNull(participant, "participant");

        setTitle("Создание чата с %s %s".formatted(
            participant.firstName(),
            participant.lastName()
        ));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel formPanel = new JPanel(new GridBagLayout());
        content.add(formPanel, BorderLayout.CENTER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;

        dhGroupCombo = new JComboBox<>(DiffieHellmanGroupId.values());
        addRow(formPanel, constraints, "Группа Диффи-Хеллмана", dhGroupCombo);

        algorithmCombo = new JComboBox<>(Algorithm.values());
        addRow(formPanel, constraints, "Алгоритм шифрования", algorithmCombo);

        cipherModeCombo = new JComboBox<>(CipherModeType.values());
        addRow(formPanel, constraints, "Режим шифрования", cipherModeCombo);

        paddingCombo = new JComboBox<>(PaddingType.values());
        addRow(formPanel, constraints, "Режим набивки", paddingCombo);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        content.add(buttons, BorderLayout.SOUTH);

        createButton = new JButton("Создать");
        createButton.addActionListener(new CreateChatActionListener());
        buttons.add(createButton);
        getRootPane().setDefaultButton(createButton);

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener((event) -> dispose());
        buttons.add(cancelButton);

        setContentPane(content);
        pack();
        setLocationRelativeTo(parent);
    }

    private void addRow(
        JPanel panel,
        GridBagConstraints constraints,
        String labelText,
        JComboBox<?> comboBox
    ) {
        GridBagConstraints labelConstraints = (GridBagConstraints) constraints.clone();
        labelConstraints.gridx = 0;
        labelConstraints.weightx = 0;
        JLabel label = new JLabel(labelText);
        panel.add(label, labelConstraints);

        GridBagConstraints comboConstraints = (GridBagConstraints) constraints.clone();
        comboConstraints.gridx = 1;
        comboConstraints.weightx = 1;
        panel.add(comboBox, comboConstraints);

        constraints.gridy++;
    }

    private void setFormEnabled(boolean enabled) {
        dhGroupCombo.setEnabled(enabled);
        algorithmCombo.setEnabled(enabled);
        cipherModeCombo.setEnabled(enabled);
        paddingCombo.setEnabled(enabled);
        createButton.setEnabled(enabled);
    }

    private class CreateChatActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            setFormEnabled(false);

            var group = (DiffieHellmanGroupId)
                requireNonNull(dhGroupCombo.getSelectedItem(), "Diffie-Hellman group");
            var algorithm = (Algorithm)
                requireNonNull(algorithmCombo.getSelectedItem(), "algorithm");
            var cipherMode = (CipherModeType)
                requireNonNull(cipherModeCombo.getSelectedItem(), "cipher mode");
            var padding = (PaddingType)
                requireNonNull(paddingCombo.getSelectedItem(), "padding mode");

            chatStore.createChat(participant.id(), group, algorithm, cipherMode, padding)
                .whenComplete((nothing, throwable) -> {
                    if (throwable == null) {
                        EventQueue.invokeLater(this::createSuccess);
                    } else {
                        EventQueue.invokeLater(() -> createFailed(throwable));
                    }
                });
        }

        private void createSuccess() {
            dispose();
        }

        private void createFailed(Throwable throwable) {
            setFormEnabled(true);

            JOptionPane.showMessageDialog(
                CreateChatDialog.this,
                throwable.getMessage(),
                "Ошибка создания чата",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
