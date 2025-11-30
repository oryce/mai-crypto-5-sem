package dora.messenger.client.ui.chat;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.ScaledImageIcon;
import dora.messenger.client.persistence.ChatFile;
import dora.messenger.client.persistence.ChatMessage;
import dora.messenger.client.persistence.ChatSession;
import dora.messenger.client.store.Computed;
import dora.messenger.client.store.chat.Chat;
import dora.messenger.client.store.chat.ChatFileStore;
import dora.messenger.client.store.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public class ChatMessageBubble extends JPanel {

    private static final Color TEXT_COLOR = Color.WHITE;

    private final Chat chat;
    private final ChatSession session;
    private final ChatFileStore fileStore;
    private final ChatMessage message;
    private final User currentUser;

    private final List<AttachmentComponent> attachments;
    private final MessageText messageText;

    public ChatMessageBubble(
        @NotNull Chat chat,
        @NotNull ChatSession session,
        @NotNull ChatFileStore fileStore,
        @NotNull ChatMessage message,
        @NotNull User currentUser
    ) {
        this.chat = requireNonNull(chat, "chat");
        this.session = requireNonNull(session, "chat session");
        this.fileStore = requireNonNull(fileStore, "file store");

        this.message = requireNonNull(message, "chat message");
        this.currentUser = requireNonNull(currentUser, "current user");

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        int row = 0;

        setBackground(byCurrentUser() ? new Color(0x6D6A5B) : new Color(0x546E7A));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        // Add attachments.
        attachments = new ArrayList<>();

        for (UUID attachmentId : message.getAttachmentIds()) {
            Computed<ChatFile> fileRef = fileStore.getById(attachmentId);
            AttachmentComponent attachment = createAttachment(fileRef, maxWidth());
            attachments.add(attachment);

            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(0, 0, 6, 0);
            constraints.weightx = 1;
            constraints.gridy = row++;
            add(attachment.component(), constraints);
        }

        // Add text (if present).
        if (!message.getContent().isBlank()) {
            messageText = new MessageText(message.getContent());
            messageText.setForeground(TEXT_COLOR);
            messageText.setMinWidth(40);
            messageText.setMaxWidth(maxWidth());
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.weightx = 0;
            constraints.gridy = row++;
            add(messageText, constraints);
        } else {
            messageText = null;
        }

        MessageTime messageTime = new MessageTime(message.getTimestamp());
        messageTime.setForeground(new Color(0xE5E7EB));
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(4, 0, 0, 0);
        constraints.weightx = 0;
        constraints.gridy = row;
        add(messageTime, constraints);

        SwingUtilities.invokeLater(this::updateWidth);
    }

    private AttachmentComponent createAttachment(Computed<ChatFile> fileRef, int initialMaxWidth) {
        ChatFile file = fileRef.get();

        if (file != null && isImageFile(file.getFilename())) {
            return new MessageImageAttachment(fileRef, initialMaxWidth);
        }

        return new MessageFileAttachment(fileRef);
    }

    private boolean isImageFile(String filename) {
        if (filename == null) return false;

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) return false;

        Set<String> imageExtensions = Set.of("bmp", "gif", "jpg", "jpeg", "png", "webp");
        String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return imageExtensions.contains(extension);
    }

    public boolean byCurrentUser() {
        return message.getSenderId().equals(currentUser.id());
    }

    public void updateWidth() {
        int maxWidth = maxWidth();

        if (messageText != null) {
            messageText.setMaxWidth(maxWidth);
        }

        for (AttachmentComponent attachment : attachments) {
            attachment.setMaxWidth(maxWidth);
        }
    }

    private int maxWidth() {
        int viewportWidth = viewportWidth();
        int maxWidth = 400;

        return viewportWidth <= 0 ? maxWidth : Math.min(maxWidth, viewportWidth - 50);
    }

    private int viewportWidth() {
        Container viewport = SwingUtilities.getAncestorOfClass(JViewport.class, this);
        return viewport != null ? viewport.getWidth() : 0;
    }

    private interface AttachmentComponent {

        JComponent component();

        void setMaxWidth(int maxWidth);
    }

    private class MessageFileAttachment extends JPanel implements AttachmentComponent {

        private final Computed<ChatFile> computedFile;

        private final FlatSVGIcon downloadIcon;
        private final FlatSVGIcon cancelIcon;
        private final FlatSVGIcon fileIcon;

        private final JButton actionButton;
        private final JProgressBar progressBar;

        public MessageFileAttachment(@NotNull Computed<ChatFile> computedFile) {
            this.computedFile = requireNonNull(computedFile, "computed file");

            downloadIcon = new FlatSVGIcon("icons/download.svg");
            cancelIcon = new FlatSVGIcon("icons/close.svg");
            fileIcon = new FlatSVGIcon("icons/file.svg");

            setOpaque(false);
            setLayout(new GridBagLayout());

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;

            actionButton = new JButton();
            actionButton.addActionListener((event) -> actionClicked());
            constraints.insets = new Insets(0, 0, 0, 8);
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridheight = 2;
            add(actionButton, constraints);

            constraints.weightx = 1.0;
            constraints.gridheight = 1;

            JLabel nameLabel = new JLabel();
            nameLabel.setForeground(TEXT_COLOR);
            nameLabel.setText(computedFile.get().getFilename());
            constraints.gridx = 1;
            constraints.gridy = 0;
            add(nameLabel, constraints);

            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(false);
            progressBar.setVisible(false);
            progressBar.setPreferredSize(new Dimension(180, 6));
            progressBar.setMaximumSize(new Dimension(180, 6));
            constraints.insets = new Insets(4, 0, 0, 0);
            constraints.gridx = 1;
            constraints.gridy = 1;
            add(progressBar, constraints);

            computedFile.observe((file) -> SwingUtilities.invokeLater(() -> update(file)));
            update(computedFile.get());
        }

        @Override
        public JComponent component() {
            return this;
        }

        @Override
        public void setMaxWidth(int maxWidth) {
        }

        private void update(ChatFile file) {
            if (file == null) return;

            // TODO (20.12.25, ~oryce):
            //   Ideally, this should change the action listener.

            if (file.getLocation() != null) {
                actionButton.setIcon(fileIcon);
            } else {
                actionButton.setIcon(downloadIcon);
            }
        }

        private void actionClicked() {
            ChatFile file = computedFile.get();

            if (file.getLocation() != null) {
                open(file);
            } else if (downloadFuture == null) {
                startDownload();
            } else {
                stopDownload();
            }
        }

        private void open(ChatFile file) {
            try {
                Desktop.getDesktop().open(new File(file.getLocation()));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    e.getMessage(),
                    "Ошибка открытия файла",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }

        private CompletableFuture<Void> downloadFuture;

        private void startDownload() {
            actionButton.setIcon(cancelIcon);
            progressBar.setVisible(true);
            progressBar.setValue(0);

            downloadFuture = fileStore.downloadFile(chat, session, computedFile, this::onDownloadProgress)
                .whenComplete((nothing, throwable) -> {
                    if (throwable == null) {
                        EventQueue.invokeLater(this::downloadFinished);
                    } else if (throwable instanceof CancellationException || downloadFuture.isCancelled()) {
                        EventQueue.invokeLater(this::downloadUnfinished);
                    } else {
                        EventQueue.invokeLater(() -> downloadFailed(throwable));
                    }
                });
        }

        private int lastPercent;

        private void onDownloadProgress(long transferredSize, long totalSize) {
            if (totalSize <= 0) return;

            int percent = (int) Math.min(100, (transferredSize * 100) / totalSize);
            if (percent == lastPercent) return;

            lastPercent = percent;
            SwingUtilities.invokeLater(() -> progressBar.setValue(percent));
        }

        private void downloadFinished() {
            downloadFuture = null;
            actionButton.setIcon(fileIcon);
            progressBar.setVisible(false);
        }

        private void downloadUnfinished() {
            downloadFuture = null;
            actionButton.setIcon(downloadIcon);
            progressBar.setVisible(false);
        }

        private void downloadFailed(Throwable throwable) {
            downloadUnfinished();

            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(MessageFileAttachment.this),
                throwable.getMessage(),
                "Ошибка загрузки файла",
                JOptionPane.ERROR_MESSAGE
            );
        }

        private void stopDownload() {
            if (downloadFuture != null) {
                downloadFuture.cancel(true);
            }
        }
    }

    private class MessageImageAttachment extends JPanel implements AttachmentComponent {

        private final CardLayout layout;
        private final MessageImageDownloadAttachment downloadAttachment;
        private final MessageImageDownloadedAttachment downloadedAttachment;

        public MessageImageAttachment(Computed<ChatFile> computedFile, int initialMaxWidth) {
            setOpaque(false);

            layout = new CardLayout();
            setLayout(layout);

            downloadAttachment = new MessageImageDownloadAttachment(computedFile);
            downloadAttachment.setMaxWidth(initialMaxWidth);
            add(downloadAttachment, "download");

            downloadedAttachment = new MessageImageDownloadedAttachment();
            downloadedAttachment.setMaxWidth(initialMaxWidth);
            add(downloadedAttachment, "downloaded");

            computedFile.observe((file) -> SwingUtilities.invokeLater(() -> update(file)));
            update(computedFile.get());
        }

        @Override
        public JComponent component() {
            return this;
        }

        @Override
        public void setMaxWidth(int maxWidth) {
            downloadAttachment.setMaxWidth(maxWidth);
            downloadedAttachment.setMaxWidth(maxWidth);
        }

        private void update(ChatFile file) {
            boolean downloaded = file != null && file.getLocation() != null;

            if (downloaded) {
                layout.show(this, "downloaded");
                downloadedAttachment.setFile(file);
            } else {
                layout.show(this, "download");
                downloadedAttachment.setFile(null);
            }

            revalidate();
            repaint();
        }
    }

    private class MessageImageDownloadAttachment extends JPanel {

        private final Computed<ChatFile> computedFile;

        private final JButton actionButton;
        private final JProgressBar progressBar;

        public MessageImageDownloadAttachment(@NotNull Computed<ChatFile> computedFile) {
            this.computedFile = requireNonNull(computedFile, "computed file");

            setBackground(new Color(0x3B424A));
            setLayout(new GridBagLayout());

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.CENTER;

            JLabel iconLabel = new JLabel(new FlatSVGIcon("icons/image.svg"));
            constraints = new GridBagConstraints();
            constraints.gridy = 1;
            add(iconLabel, constraints);

            JLabel nameLabel = new JLabel();
            nameLabel.setForeground(TEXT_COLOR);
            nameLabel.setText(computedFile.get().getFilename());
            constraints.insets = new Insets(8, 0, 0, 0);
            constraints.gridy = 2;
            add(nameLabel, constraints);

            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(false);
            progressBar.setVisible(false);
            progressBar.setPreferredSize(new Dimension(180, 6));
            progressBar.setMaximumSize(new Dimension(180, 6));
            constraints.insets = new Insets(8, 0, 0, 0);
            constraints.gridy = 3;
            add(progressBar, constraints);

            actionButton = new JButton("Загрузить");
            actionButton.addActionListener((event) -> actionClicked());
            constraints.insets = new Insets(8, 0, 0, 0);
            constraints.gridy = 4;
            add(actionButton, constraints);
        }

        public void setMaxWidth(int maxWidth) {
            final int width = 320;
            final int height = 200;

            setMinimumSize(new Dimension(0, height));
            setMaximumSize(new Dimension(maxWidth, height));
            setPreferredSize(new Dimension(Math.min(width, maxWidth), height));

            revalidate();
            repaint();
        }

        private CompletableFuture<Void> downloadFuture;

        private void actionClicked() {
            if (downloadFuture == null) {
                startDownload();
            } else {
                stopDownload();
            }
        }

        private void startDownload() {
            actionButton.setText("Отмена");
            progressBar.setVisible(true);
            progressBar.setValue(0);

            downloadFuture = fileStore.downloadFile(chat, session, computedFile, this::onDownloadProgress)
                .whenComplete((nothing, throwable) -> {
                    if (throwable == null
                        || throwable instanceof CancellationException
                        || downloadFuture.isCancelled()
                    ) {
                        EventQueue.invokeLater(this::downloadEnded);
                    } else {
                        EventQueue.invokeLater(() -> downloadFailed(throwable));
                    }
                });
        }

        private int lastPercent;

        private void onDownloadProgress(long transferredSize, long totalSize) {
            if (totalSize <= 0) return;

            int percent = (int) Math.min(100, (transferredSize * 100) / totalSize);
            if (percent == lastPercent) return;

            lastPercent = percent;
            SwingUtilities.invokeLater(() -> progressBar.setValue(percent));
        }

        private void downloadEnded() {
            downloadFuture = null;

            actionButton.setText("Загрузить");
            progressBar.setVisible(false);
        }

        private void downloadFailed(Throwable throwable) {
            downloadEnded();

            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(MessageImageDownloadAttachment.this),
                throwable.getMessage(),
                "Ошибка загрузки изображения",
                JOptionPane.ERROR_MESSAGE
            );
        }

        private void stopDownload() {
            if (downloadFuture != null) {
                downloadFuture.cancel(true);
            }
        }
    }

    private class MessageImageDownloadedAttachment extends JPanel {

        private final JLabel imageLabel;

        public MessageImageDownloadedAttachment() {
            setOpaque(false);
            add(imageLabel = new JLabel());
        }

        private int maxWidth;

        public void setMaxWidth(int maxWidth) {
            this.maxWidth = Math.max(0, maxWidth);
            resizeImage();
        }

        private BufferedImage downloadedImage;

        public void setFile(@Nullable ChatFile file) {
            if (file == null) {
                downloadedImage = null;
                imageLabel.setIcon(null);
                return;
            }

            BufferedImage newImage = readImage(file);

            if (newImage != null) {
                downloadedImage = newImage;
                resizeImage();
            }
        }

        private @Nullable BufferedImage readImage(ChatFile file) {
            if (file.getLocation() == null) {
                // File not downloaded yet.
                return null;
            }

            try {
                return ImageIO.read(new File(file.getLocation()));
            } catch (IOException e) {
                return null;
            }
        }

        private int lastScaledWidth;

        private void resizeImage() {
            if (downloadedImage == null) return;

            int scaledWidth = Math.min(maxWidth, downloadedImage.getWidth());
            if (scaledWidth == lastScaledWidth) return;

            float scaleFactor = (float) scaledWidth / downloadedImage.getWidth();
            int scaledHeight = Math.round(downloadedImage.getHeight() * scaleFactor);

            imageLabel.setIcon(new ScaledImageIcon(
                new ImageIcon(downloadedImage),
                scaledWidth,
                scaledHeight
            ));
            lastScaledWidth = scaledWidth;

            revalidate();
            repaint();
        }
    }

    private class MessageText extends JComponent {

        private final JTextArea textArea;

        private int minWidth;
        private int maxWidth;

        public MessageText(String text) {
            textArea = new JTextArea(text);

            textArea.setBorder(null);
            textArea.setEditable(false);
            textArea.setFocusable(true);
            textArea.setOpaque(false);
            textArea.setMargin(new Insets(0, 0, 0, 0));

            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(false); // Wrap long words

            add(textArea);
        }

        public void setForeground(Color color) {
            textArea.setForeground(color);
        }

        public void setMinWidth(int minWidth) {
            this.minWidth = minWidth;
            revalidate();
        }

        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            revalidate();
        }

        @Override
        public Dimension getPreferredSize() {
            Insets insets = getInsets();

            // Clamp text width to [minWidth; limit].
            int limit = Math.max(1, maxWidth - insets.left - insets.right);
            int width = Math.clamp(textWidth(), minWidth, limit);

            // Resize message.
            textArea.setSize(width, Integer.MAX_VALUE);

            // Return updated size.
            Dimension textSize = textArea.getPreferredSize();

            return new Dimension(
                textSize.width + insets.left + insets.right,
                textSize.height + insets.top + insets.bottom
            );
        }

        private int textWidth() {
            FontMetrics fontMetrics = textArea.getFontMetrics(textArea.getFont());

            return Arrays.stream(message.getContent().split("\n"))
                .map((line) -> fontMetrics.stringWidth(line) + 2)
                .max(Comparator.comparingInt((width) -> width))
                .orElse(0);
        }
    }

    private class MessageTime extends JLabel {

        public MessageTime(Instant instant) {
            var dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            var formattedTime = DateTimeFormatter.ofPattern("H:mm").format(dateTime);
            setText(formattedTime);
        }
    }
}
