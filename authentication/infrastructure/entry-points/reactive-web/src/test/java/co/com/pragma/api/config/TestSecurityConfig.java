package co.com.pragma.api.config;

import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.jwt.JwtService;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@TestConfiguration
@EnableWebFluxSecurity
@AllArgsConstructor
@Component
public class TestSecurityConfig {

    private final JwtService jwtService;

    @BeforeEach
    void setup() {
        when(jwtService.extractRoleId("admin-token")).thenReturn(RolEnum.ADMIN.getId());
        when(jwtService.extractRoleId("assessor-token")).thenReturn(RolEnum.ASSESSOR.getId());
        when(jwtService.extractRoleId("client-token")).thenReturn(RolEnum.CLIENT.getId());
    }

    @Bean
    public SecurityWebFilterChain testSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> {
                    // Public endpoints
                    auth.pathMatchers("/api/v1/login").permitAll();
                    auth.pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();

                    // Endpoints USERS simulando roles
                    auth.pathMatchers("/api/v1/users").access(this::isAdminOrAsesor);
                    auth.pathMatchers("/api/v1/users/**").access(this::isAdminOrAsesor);

                    // Todos los demás endpoints permitidos para tests
                    auth.anyExchange().permitAll();
                })
                .build();
    }

    private Mono<AuthorizationDecision> isAdminOrAsesor(Mono<org.springframework.security.core.Authentication> authMono,
                                                        org.springframework.security.web.server.authorization.AuthorizationContext context) {
        return authMono
                .map(auth -> {
                    Object credentials = auth.getCredentials();
                    if (credentials == null) return new AuthorizationDecision(true);
                    String token = (String) credentials;
                    boolean allowed = jwtService.extractRoleId(token).equals(RolEnum.ADMIN.getId()) ||
                            jwtService.extractRoleId(token).equals(RolEnum.ASSESSOR.getId());
                    return new AuthorizationDecision(allowed);
                })
                .defaultIfEmpty(new AuthorizationDecision(true));
    }
}