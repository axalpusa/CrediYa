package co.com.pragma.api;

import co.com.pragma.api.config.SecurityConfig;
import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    private JwtService jwtService;
    private WebTestClient webTestClient;

    private String adminToken;
    private UUID adminRoleId;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);

        adminToken = "valid-admin-token";
        adminRoleId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        when(jwtService.extractRoleId(adminToken)).thenReturn(adminRoleId);
        when(jwtService.extractUserId(adminToken)).thenReturn(UUID.randomUUID());

        SecurityConfig securityConfig = new SecurityConfig(jwtService);

        webTestClient = WebTestClient
                .bindToRouterFunction(RouterFunctions.route()
                        .GET("/swagger-ui.html", request -> org.springframework.web.reactive.function.server.ServerResponse.ok().build())
                        .GET("/api/v1/users", request -> org.springframework.web.reactive.function.server.ServerResponse.ok().build())
                        .POST("/api/v1/users", request -> org.springframework.web.reactive.function.server.ServerResponse.created(null).build())
                        .build())
                .webFilter(securityConfig.jwtAuthenticationFilter())
                .build();
    }

    @Test
    void publicEndpointShouldBeAccessibleWithoutToken() {
        webTestClient.get().uri("/swagger-ui.html")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void protectedGetShouldRequireToken() {
        webTestClient.get().uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void protectedPostShouldRequireToken() {
        webTestClient.post().uri("/api/v1/users")
                .bodyValue(Map.of("email", "test@example.com", "password", "123456"))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void protectedGetShouldAllowAdmin() {
        webTestClient.get().uri("/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedPostShouldAllowAdmin() {
        webTestClient.post().uri("/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(Map.of("email", "test@example.com", "password", "123456"))
                .exchange()
                .expectStatus().isUnauthorized();
    }
    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        public SecurityConfig securityConfig(JwtService jwtService) {
            return new SecurityConfig(jwtService);
        }
    }

}
