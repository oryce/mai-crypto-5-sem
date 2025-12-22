package dora.messenger.client.ui.chat;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import dora.messenger.client.persistence.ChatFile;
import dora.messenger.client.persistence.ChatSession;
import dora.messenger.client.store.chat.Chat;
import dora.messenger.client.store.chat.ChatFileStore;
import dora.messenger.client.store.chat.ChatMessageStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public class ChatMessageInput extends JPanel {

    private static final Logger LOGGER = LogManager.getLogger(ChatMessageInput.class);

    private final Chat chat;
    private final ChatSession chatSession;
    private final ChatMessageStore chatMessageStore;
    private final ChatFileStore chatFileStore;

    private final JTextField inputField;
    private final JButton sendButton;
    private final JPanel attachmentsPanel;

    public ChatMessageInput(
        @NotNull Chat chat,
        @NotNull ChatSession chatSession,
        @NotNull ChatMessageStore chatMessageStore,
        @NotNull ChatFileStore chatFileStore
    ) {
        this.chat = requireNonNull(chat, "chat");
        this.chatSession = requireNonNull(chatSession, "chat session");
        this.chatMessageStore = requireNonNull(chatMessageStore, "chat message store");
        this.chatFileStore = requireNonNull(chatFileStore, "chat file store");

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        JButton attachButton = new JButton(new FlatSVGIcon("icons/attach.svg"));
        attachButton.addActionListener(new AttachActionListener());
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(attachButton, constraints);

        inputField = new JTextField();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 8, 0, 0);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(inputField, constraints);

        sendButton = new JButton("Отправить");
        sendButton.addActionListener(new SendActionListener());
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 8, 0, 0);
        constraints.weightx = 0;
        constraints.gridx = 2;
        add(sendButton, constraints);

        attachmentsPanel = new JPanel();
        attachmentsPanel.setLayout(new BoxLayout(attachmentsPanel, BoxLayout.Y_AXIS));
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(6, 0, 0, 0);
        constraints.weightx = 1;
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = 1;
        add(attachmentsPanel, constraints);

        updateAttachmentsVisibility();
    }

    private void addAttachment(Path filePath) {
        attachmentsPanel.add(new AttachmentItem(filePath));
        updateAttachmentsVisibility();
    }

    private void removeAttachment(AttachmentItem item) {
        attachmentsPanel.remove(item);
        updateAttachmentsVisibility();
    }

    private void removeAllAttachments() {
        attachmentsPanel.removeAll();
        updateAttachmentsVisibility();
    }

    private void updateAttachmentsVisibility() {
        attachmentsPanel.setVisible(attachmentsPanel.getComponentCount() > 0);
        attachmentsPanel.revalidate();
        attachmentsPanel.repaint();
    }

    private class AttachActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();

            int result = fileChooser.showOpenDialog(
                SwingUtilities.getWindowAncestor(ChatMessageInput.this)
            );

            if (result == JFileChooser.APPROVE_OPTION) {
                addAttachment(fileChooser.getSelectedFile().toPath());
            }
        }
    }

    private class SendActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            sendButton.setEnabled(false);

            List<ChatFile> attachments = new ArrayList<>();

            for (Component component : attachmentsPanel.getComponents()) {
                if (component instanceof AttachmentItem attachment) {
                    attachment.uploadedFile().ifPresent(attachments::add);
                    attachment.setEnabled(false);
                }
            }

            chatMessageStore.sendMessage(chat, chatSession, inputField.getText(), attachments)
                .whenComplete((nothing, throwable) -> {
                    if (throwable == null) {
                        SwingUtilities.invokeLater(this::sendSuccess);
                    } else {
                        SwingUtilities.invokeLater(() -> sendFailed(throwable));
                    }
                });
        }

        private void sendSuccess() {
            sendButton.setEnabled(true);
            inputField.setText("");
            removeAllAttachments();
        }

        private void sendFailed(Throwable throwable) {
            sendButton.setEnabled(true);

            for (Component attachment : attachmentsPanel.getComponents()) {
                attachment.setEnabled(true);
            }

            LOGGER.error("Cannot send message", throwable);

            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(ChatMessageInput.this),
                "Произошла ошибка при отправке сообщения",
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private class AttachmentItem extends JPanel {

        private final JProgressBar progressBar;
        private final JPanel progressHolder;
        private final JButton removeButton;

        private final CompletableFuture<ChatFile> uploadFuture;

        AttachmentItem(Path filePath) {
            setLayout(new GridBagLayout());

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(2, 0, 2, 0);

            JLabel nameLabel = new JLabel(filePath.getFileName().toString());
            constraints.fill = GridBagConstraints.NONE;
            constraints.weightx = 0;
            constraints.gridx = 0;
            add(nameLabel, constraints);

            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);

            progressHolder = new JPanel(new BorderLayout());
            progressHolder.add(progressBar, BorderLayout.CENTER);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(2, 8, 2, 8);
            constraints.weightx = 1;
            constraints.gridx = 1;
            add(progressHolder, constraints);

            removeButton = new JButton(new FlatSVGIcon("icons/close.svg"));
            removeButton.addActionListener((event) -> remove());
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(2, 0, 2, 0);
            constraints.weightx = 0;
            constraints.gridx = 2;
            add(removeButton, constraints);

            uploadFuture = chatFileStore.uploadFile(chat, chatSession, filePath, this::onUploadProgress);

            uploadFuture.whenComplete((result, throwable) -> {
                if (throwable == null) {
                    SwingUtilities.invokeLater(this::uploadSuccess);
                } else if (throwable instanceof CancellationException) {
                    SwingUtilities.invokeLater(this::uploadCancelled);
                } else {
                    SwingUtilities.invokeLater(() -> uploadFailed(throwable));
                }
            });
        }

        public Optional<ChatFile> uploadedFile() {
            return Optional.ofNullable(uploadFuture.getNow(null));
        }

        @Override
        public void setEnabled(boolean enabled) {
            removeButton.setEnabled(enabled);
        }

        private int lastPercent;

        private void onUploadProgress(long transferredSize, long totalSize) {
            if (totalSize <= 0) {
                return;
            }

            int percent = (int) Math.min(100, (transferredSize * 100) / totalSize);
            if (percent == lastPercent) return;

            lastPercent = percent;
            SwingUtilities.invokeLater(() -> progressBar.setValue(percent));
        }

        private void uploadSuccess() {
            progressBar.setVisible(false);
            progressHolder.revalidate();
            progressHolder.repaint();
        }

        private void uploadCancelled() {
            removeAttachment(this);
        }

        private void uploadFailed(Throwable throwable) {
            LOGGER.error("Cannot upload file", throwable);

            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(AttachmentItem.this),
                "Произошла ошибка при загрузке файла",
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );

            removeAttachment(this);
        }

        private void remove() {
            if (uploadFuture.isDone()) {
                removeAttachment(AttachmentItem.this);
                return;
            }

            removeButton.setEnabled(false);
            uploadFuture.cancel(true);
        }
    }
}
