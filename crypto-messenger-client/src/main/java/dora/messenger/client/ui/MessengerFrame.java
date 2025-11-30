package dora.messenger.client.ui;

import jakarta.inject.Singleton;

import javax.swing.JFrame;

@Singleton
public class MessengerFrame extends JFrame {

    public MessengerFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Dora Messenger");
        setSize(640, 480);
        setLocationRelativeTo(null);
    }
}
