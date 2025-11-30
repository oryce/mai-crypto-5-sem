package dora.messenger.client.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoadingView extends JPanel {

    public LoadingView() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;

        JLabel label = new JLabel("Загрузка...");
        constraints.gridy = 0;
        add(label, constraints);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        constraints.gridy = 1;
        constraints.insets = new Insets(8, 0, 0, 0);
        add(progressBar, constraints);
    }
}
