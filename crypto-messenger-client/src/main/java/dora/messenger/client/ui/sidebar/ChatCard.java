package dora.messenger.client.ui.sidebar;

import dora.messenger.client.store.chat.Chat;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.util.Objects.requireNonNull;

public class ChatCard extends JPanel {

    private final JPopupMenu popupMenu;
    private final Runnable onOpen;
    private final Runnable onDelete;

    public ChatCard(
        @NotNull Chat chat,
        boolean active,
        @NotNull Runnable onOpen,
        @NotNull Runnable onDelete
    ) {
        requireNonNull(chat, "chat");
        requireNonNull(onOpen, "open action");
        requireNonNull(onDelete, "delete action");

        this.onOpen = onOpen;
        this.onDelete = onDelete;

        setLayout(new GridBagLayout());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setOpaque(true);

        GridBagConstraints constraints = new GridBagConstraints();

        JLabel nameLabel = new JLabel(chat.name());
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(nameLabel, constraints);

        setActive(active);

        popupMenu = new JPopupMenu();
        addMouseListener(new PopupMouseListener());

        JMenuItem deleteItem = new JMenuItem("Удалить");
        deleteItem.addActionListener(new PopupDeleteActionListener());
        popupMenu.add(deleteItem);

        addMouseListener(new OpenMouseListener());
    }

    private void setActive(boolean active) {
        Color background = UIManager.getColor("Panel.background");
        Color selectedBackground = UIManager.getColor("Button.hoverBackground");
        setBackground(active ? selectedBackground : background);

        Border border = BorderFactory.createEmptyBorder(6, 8, 6, 8);
        Border selectedBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Button.hoverBorderColor")),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        );
        setBorder(active ? selectedBorder : border);
    }

    private class OpenMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) onOpen.run();
        }
    }

    private class PopupMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isPopupTrigger()) showMenu(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) showMenu(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) showMenu(e);
        }

        private void showMenu(MouseEvent e) {
            popupMenu.show(ChatCard.this, e.getX(), e.getY());
        }

    }

    private class PopupDeleteActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            onDelete.run();
        }
    }
}
