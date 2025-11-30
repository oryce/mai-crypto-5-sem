package dora.messenger.client;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;

public class Main {

    public static void main(String[] args) {
        FlatLightLaf.setup();

        JFrame frame = new JFrame("Dora Messenger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new BorderLayout());
        frame.add(new JLabel("Hello, world!"), BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
