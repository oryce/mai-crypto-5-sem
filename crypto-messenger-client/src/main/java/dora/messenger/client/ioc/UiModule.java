package dora.messenger.client.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import dora.messenger.client.ui.chat.ChatView;
import dora.messenger.client.ui.router.ViewFactory;

public class UiModule extends AbstractModule {

    @Override
    protected void configure() {
        install(
            new FactoryModuleBuilder()
                .implement(ChatView.class, ChatView.class)
                .build(ViewFactory.class)
        );
    }
}
