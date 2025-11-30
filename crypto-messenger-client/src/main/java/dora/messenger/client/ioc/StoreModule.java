package dora.messenger.client.ioc;

import com.google.inject.AbstractModule;
import dora.messenger.client.store.chat.ChatFileStore;
import dora.messenger.client.store.chat.ChatMessageStore;
import dora.messenger.client.store.chat.ChatSessionStore;
import dora.messenger.client.store.chat.ChatStore;
import dora.messenger.client.store.contact.ContactRequestStore;
import dora.messenger.client.store.contact.ContactStore;
import dora.messenger.client.store.session.SessionStore;
import dora.messenger.client.store.user.UserStore;

public class StoreModule extends AbstractModule {

    @Override
    protected void configure() {
        // Stores are bound eagerly to allow them to register event listeners
        // before the WebSocket connection is established.

        bind(ChatStore.class).asEagerSingleton();
        bind(ChatFileStore.class).asEagerSingleton();
        bind(ChatSessionStore.class).asEagerSingleton();
        bind(ChatMessageStore.class).asEagerSingleton();
        bind(ContactStore.class).asEagerSingleton();
        bind(ContactRequestStore.class).asEagerSingleton();
        bind(SessionStore.class).asEagerSingleton();
        bind(UserStore.class).asEagerSingleton();
    }
}
