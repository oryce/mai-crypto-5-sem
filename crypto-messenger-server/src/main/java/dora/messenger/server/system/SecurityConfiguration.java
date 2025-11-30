package dora.messenger.server.system;

import dora.messenger.server.session.SessionAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final SessionAuthenticationFilter authenticationFilter;
    private final Environment environment;

    public SecurityConfiguration(
        SessionAuthenticationFilter authenticationFilter,
        Environment environment
    ) {
        this.authenticationFilter = authenticationFilter;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement((sessions) ->
            sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests((requests) -> {
            requests.requestMatchers(HttpMethod.POST, "/sessions", "/users").permitAll();

            // Development endpoints may only be accessible with the profile enabled.
            var developmentMatchers = requests.requestMatchers(
                "/actuator/**",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            );
            if (isDevelopment()) {
                developmentMatchers.permitAll();
            } else {
                developmentMatchers.denyAll();
            }

            requests.anyRequest().authenticated();
        });

        http.csrf((csrf) -> csrf.disable());
        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private boolean isDevelopment() {
        return environment.matchesProfiles("development");
    }
}
