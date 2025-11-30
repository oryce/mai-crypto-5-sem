package dora.messenger.server.session;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class SessionConfiguration {

    private static final int MIN_SECRET_LENGTH = 64;

    @Bean
    public Algorithm sessionJwtAlgorithm(@Value("${crypto.session.secret}") String secret) {
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalArgumentException(
                "Secret should be at least %d characters long".formatted(MIN_SECRET_LENGTH)
            );
        }

        return Algorithm.HMAC512(secret);
    }

    @Bean
    public Duration sessionLifetime(@Value("${crypto.session.lifetime}") String lifetime) {
        return Duration.parse(lifetime);
    }
}
