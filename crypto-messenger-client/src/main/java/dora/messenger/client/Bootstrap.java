package dora.messenger.client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dora.messenger.client.ioc.ApiModule;
import dora.messenger.client.ioc.PersistenceModule;
import dora.messenger.client.ioc.StoreModule;
import dora.messenger.client.ioc.UiModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Bootstrap {

    private static final Logger LOGGER = LogManager.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        LOGGER.debug("Starting application...");

        Injector injector = Guice.createInjector(
            new ApiModule(),
            new PersistenceModule(),
            new StoreModule(),
            new UiModule()
        );

        injector.getInstance(MessengerClientApplication.class).run();
    }
}
