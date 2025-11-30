package dora.messenger.client.ui.router;

import dora.messenger.client.store.chat.Chat;
import dora.messenger.client.ui.SplitView;

import java.awt.Container;

public record ChatRoute(Chat chat) implements Route {

    @Override
    public Container contentPane(Router router) {
        return new SplitView(router.sidebar(), router.viewFactory().chat(chat));
    }
}
