package dora.messenger.client.api;

import feign.RequestInterceptor;

import java.util.Optional;

public interface AuthenticationProvider {

    Optional<Authentication> authentication();

    default RequestInterceptor feignInterceptor() {
        return (request) -> authentication().ifPresent(
            (authentication) -> authentication.applyToFeign(request)
        );
    }
}
