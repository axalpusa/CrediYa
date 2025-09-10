package co.com.pragma.api.config;

import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.jwt.JwtService;
import co.com.pragma.usecase.authentication.AuthUseCase;
import co.com.pragma.usecase.rol.RolUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.reactive.TransactionalOperator;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@AutoConfigureWebTestClient
@Import({
        SecurityConfig.class,
        SecurityConfigIntegrationTest.JwtServiceTestConfig.class,
        SecurityConfigIntegrationTest.AuthUseCaseTestConfig.class,
        SecurityConfigIntegrationTest.TransactionalOperatorTestConfig.class,
        SecurityConfigIntegrationTest.RolUseCaseTestConfig.class
})
class SecurityConfigIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtService jwtService;

    @TestConfiguration
    static class JwtServiceTestConfig {
        @Bean
        JwtService jwtService() {
            return Mockito.mock ( JwtService.class );
        }
    }

    @TestConfiguration
    static class AuthUseCaseTestConfig {
        @Bean
        public AuthUseCase authUseCase() {
            return Mockito.mock ( AuthUseCase.class );
        }
    }

    @TestConfiguration
    static class TransactionalOperatorTestConfig {
        @Bean
        public TransactionalOperator transactionalOperator() {
            return Mockito.mock ( TransactionalOperator.class );
        }
    }

    @TestConfiguration
    static class RolUseCaseTestConfig {
        @Bean
        public RolUseCase rolUseCase() {
            return Mockito.mock ( RolUseCase.class );
        }
    }

    @Test
    void publicEndpointsAccessible() {
        webTestClient.post ( ).uri ( ApiPaths.LOGIN )
                .exchange ( )
                .expectStatus ( ).isOk ( );
    }

    @Test
    void protectedEndpointsRequireAuth() {
        webTestClient.get ( ).uri ( ApiPaths.USERSALL )
                .exchange ( )
                .expectStatus ( ).isUnauthorized ( );
    }

    @Test
    void adminCanAccessUserEndpoints() {
        when ( jwtService.extractRoleId ( anyString ( ) ) ).thenReturn ( RolEnum.ADMIN.getId ( ) );

        webTestClient.get ( ).uri ( ApiPaths.USERSALL )
                .header ( HttpHeaders.AUTHORIZATION, "Bearer faketoken" )
                .exchange ( )
                .expectStatus ( ).isUnauthorized ( );
    }

    @Test
    void clientCanOnlyAccessOwnUser() {
        UUID userId = UUID.randomUUID ( );

        when ( jwtService.extractRoleId ( anyString ( ) ) ).thenReturn ( RolEnum.CLIENT.getId ( ) );
        when ( jwtService.extractUserId ( anyString ( ) ) ).thenReturn ( userId );

        webTestClient.get ( )
                .uri ( ApiPaths.USERSBYID, userId )
                .header ( HttpHeaders.AUTHORIZATION, "Bearer faketoken" )
                .exchange ( )
                .expectStatus ( ).isUnauthorized ( );

        webTestClient.get ( )
                .uri ( ApiPaths.USERSBYID, UUID.randomUUID ( ) )
                .header ( HttpHeaders.AUTHORIZATION, "Bearer faketoken" )
                .exchange ( )
                .expectStatus ( ).isUnauthorized ( );
    }
}