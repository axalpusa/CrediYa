package co.com.pragma.api;

import co.com.pragma.api.config.GlobalErrorHandler;
import co.com.pragma.api.handler.AuthHandler;
import co.com.pragma.api.jwt.JwtService;
import co.com.pragma.api.routerrest.AuthRouterRest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RouterAuthHandlerValidateTokenWebTest {

    private WebTestClient webTestClient;
    private JwtService jwtService;
    private AuthHandler authHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jwtService = mock ( JwtService.class );
        authHandler = new AuthHandler ( null, jwtService, null, null );
        objectMapper = new ObjectMapper ( );
        AuthRouterRest authRouterRest = new AuthRouterRest ( );
        GlobalErrorHandler globalErrorHandler = new GlobalErrorHandler ( objectMapper );
        webTestClient = WebTestClient
                .bindToRouterFunction ( authRouterRest.authRouterFunction ( authHandler ) )
                .handlerStrategies ( HandlerStrategies.builder ( )
                        .exceptionHandler ( globalErrorHandler )
                        .build ( ) )
                .build ( );
    }

    @Test
    @DisplayName("GET /api/v1/auth/validate - valid token returns 200")
    void validateTokenSuccess() {
        String token = "valid-token";
        UUID idUser = UUID.randomUUID ( );
        UUID idRol = UUID.randomUUID ( );

        when ( jwtService.isTokenValid ( token ) ).thenReturn ( true );
        when ( jwtService.extractUserId ( token ) ).thenReturn ( idUser );
        when ( jwtService.extractRoleId ( token ) ).thenReturn ( idRol );

        webTestClient.get ( )
                .uri ( "/api/v1/auth/validate" )
                .header ( HttpHeaders.AUTHORIZATION, "Bearer " + token )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectHeader ( ).contentType ( MediaType.APPLICATION_JSON )
                .expectBody ( )
                .jsonPath ( "$.idUser" ).isEqualTo ( idUser.toString ( ) )
                .jsonPath ( "$.idRol" ).isEqualTo ( idRol.toString ( ) )
                .jsonPath ( "$.token" ).isEqualTo ( token );
    }

    @Test
    @DisplayName("GET /api/v1/auth/validate - missing header returns 400")
    void validateTokenMissingHeader() {
        webTestClient.get ( )
                .uri ( "/api/v1/auth/validate" )
                .exchange ( )
                .expectStatus ( ).isBadRequest ( )
                .expectBody ( )
                .jsonPath ( "$.message" ).isEqualTo ( "Validation Error" )
                .jsonPath ( "$.details" ).isEqualTo ( "Missing or invalid Authorization header" );
    }

    @Test
    @DisplayName("GET /api/v1/auth/validate - invalid token returns 401")
    void validateTokenInvalid() {
        String token = "invalid-token";
        when ( jwtService.isTokenValid ( token ) ).thenReturn ( false );

        webTestClient.get ( )
                .uri ( "/api/v1/auth/validate" )
                .header ( HttpHeaders.AUTHORIZATION, "Bearer " + token )
                .exchange ( )
                .expectStatus ( ).isUnauthorized ( )
                .expectBody ( )
                .jsonPath ( "$.message" ).isEqualTo ( "Unauthorized" )
                .jsonPath ( "$.details" ).isEqualTo ( "Invalid or expired token" );
    }
}