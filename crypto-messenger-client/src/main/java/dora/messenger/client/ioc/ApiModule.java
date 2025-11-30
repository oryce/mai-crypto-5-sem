package dora.messenger.client.ioc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dora.messenger.client.api.AuthenticationProvider;
import dora.messenger.client.api.ChatApi;
import dora.messenger.client.api.ChatFileApi;
import dora.messenger.client.api.ChatFileApiImpl;
import dora.messenger.client.api.ChatMessageApi;
import dora.messenger.client.api.ChatSessionApi;
import dora.messenger.client.api.ContactApi;
import dora.messenger.client.api.ContactRequestApi;
import dora.messenger.client.api.SessionApi;
import dora.messenger.client.api.SessionAuthenticationProvider;
import dora.messenger.client.api.UserApi;

import java.net.http.HttpClient;

public class ApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AuthenticationProvider.class).to(SessionAuthenticationProvider.class).in(Singleton.class);
        bind(HttpClient.class).toInstance(HttpClient.newHttpClient());

        bind(ChatFileApi.class).to(ChatFileApiImpl.class);
    }

    @Provides
    @Singleton
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Provides
    @Singleton
    ChatApi chatApi(ApiFactory factory) {
        return factory.createClient(ChatApi.class);
    }

    @Provides
    @Singleton
    ChatSessionApi chatSessionApi(ApiFactory factory) {
        return factory.createClient(ChatSessionApi.class);
    }

    @Provides
    @Singleton
    ChatMessageApi chatMessageApi(ApiFactory factory) {
        return factory.createClient(ChatMessageApi.class);
    }

    @Provides
    @Singleton
    ContactApi contactApi(ApiFactory factory) {
        return factory.createClient(ContactApi.class);
    }

    @Provides
    @Singleton
    ContactRequestApi contactRequestApi(ApiFactory factory) {
        return factory.createClient(ContactRequestApi.class);
    }

    @Provides
    @Singleton
    SessionApi sessionApi(ApiFactory factory) {
        return factory.createClient(SessionApi.class);
    }

    @Provides
    @Singleton
    UserApi userApi(ApiFactory factory) {
        return factory.createClient(UserApi.class);
    }
}
