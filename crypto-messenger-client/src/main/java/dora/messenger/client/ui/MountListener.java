package dora.messenger.client.ui;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public abstract class MountListener implements AncestorListener {

    private boolean mounted = false;

    public abstract void componentMounted();

    @Override
    public void ancestorAdded(AncestorEvent event) {
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
        if (mounted) return;

        componentMounted();
        mounted = true;
    }
}
