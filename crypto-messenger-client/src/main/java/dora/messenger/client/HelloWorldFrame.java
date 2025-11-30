package dora.messenger.client;

import org.springframework.stereotype.Component;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;

@Component
public class HelloWorldFrame extends JFrame {

    public HelloWorldFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLayout(new BorderLayout());

        add(new JLabel("Hello, world!"), BorderLayout.CENTER);
    }
}
