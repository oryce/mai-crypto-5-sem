package dora.messenger.client.ui;

import javax.swing.JSplitPane;
import java.awt.Component;

public class SplitView extends JSplitPane {

    private int maxLeftWidth = 240;

    public SplitView(Component leftComponent, Component rightComponent) {
        super(JSplitPane.HORIZONTAL_SPLIT, leftComponent, rightComponent);

        setDividerLocation(200);

        addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, (event) -> {
            int location = getDividerLocation();
            if (location > maxLeftWidth) setDividerLocation(maxLeftWidth);
        });
    }

    public void setMaxLeftWidth(int maxLeftWidth) {
        this.maxLeftWidth = maxLeftWidth;
    }
}
