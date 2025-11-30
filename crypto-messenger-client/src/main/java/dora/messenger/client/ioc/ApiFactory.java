package dora.messenger.client.ioc;

import com.fasterxml.jackson.databind.ObjectMapper;
import dora.messenger.client.api.AuthenticationProvider;
import feign.AsyncFeign;
import feign.http2client.Http2Client;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpClient;

import static java.util.Objects.requireNonNull;

@Singleton
public class ApiFactory {

    private final Provider<HttpClient> clientProvider;
    private final Provider<ObjectMapper> mapperProvider;
    private final AuthenticationProvider authenticationProvider;

    @Inject
    public ApiFactory(
        @NotNull Provider<HttpClient> clientProvider,
        @NotNull Provider<ObjectMapper> mapperProvider,
        @NotNull AuthenticationProvider authenticationProvider
    ) {
        this.clientProvider = requireNonNull(clientProvider, "HTTP client provider");
        this.mapperProvider = requireNonNull(mapperProvider, "object mapper provider");
        this.authenticationProvider = requireNonNull(authenticationProvider, "authentication provider");
    }

    <T> T createClient(Class<T> clientClass) {
        HttpClient client = clientProvider.get();
        ObjectMapper mapper = mapperProvider.get();

        AsyncFeign.AsyncBuilder<?> authorization = AsyncFeign.builder()
            .client(new Http2Client(client))
            .encoder(new JacksonEncoder(mapper))
            .decoder(new JacksonDecoder(mapper))
            .requestInterceptor(authenticationProvider.feignInterceptor());

        // TODO (16.12.25, ~oryce):
        //   Don't hardcode base URI.
        String baseUri = "http://localhost:8080";

        return authorization.target(clientClass, baseUri);
    }
}
