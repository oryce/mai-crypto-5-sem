package dora.messenger.client.ui.router;

import dora.messenger.client.ui.SplitView;

import java.awt.Container;

@FunctionalInterface
public interface Route {

    Container contentPane(Router router);

    Route LOGIN = Router::loginView;

    Route REGISTER = Router::registerView;

    Route CONTACTS = (router) -> new SplitView(router.sidebar(), router.contactView());
}
