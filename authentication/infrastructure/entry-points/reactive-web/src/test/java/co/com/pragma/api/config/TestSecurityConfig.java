package co.com.pragma.api.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;

@TestConfiguration
@EnableWebFluxSecurity
@AllArgsConstructor
@Component
public class TestSecurityConfig {

    @Bean
    public SecurityWebFilterChain testSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange ( exchanges -> exchanges
                        .anyExchange ( ).permitAll ( )
                )
                .csrf ( ServerHttpSecurity.CsrfSpec::disable )
                .build ( );
    }

}