package co.com.pragma.api;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.config.GlobalErrorHandler;
import co.com.pragma.api.dto.request.AuthRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.handler.AuthHandler;
import co.com.pragma.api.jwt.JwtService;
import co.com.pragma.api.routerrest.AuthRouterRest;
import co.com.pragma.model.auth.Auth;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.authentication.AuthUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouterAuthHandlerTest {

    private WebTestClient webTestClient;
    private AuthUseCase authUseCase;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private TransactionalAdapter transactionalAdapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        authUseCase = mock ( AuthUseCase.class );
        jwtService = mock ( JwtService.class );
        passwordEncoder = mock ( PasswordEncoder.class );
        transactionalAdapter = mock ( TransactionalAdapter.class );
        objectMapper = new ObjectMapper();

        AuthHandler authHandler = new AuthHandler ( authUseCase, jwtService, passwordEncoder, transactionalAdapter );
        AuthRouterRest authRouterRest = new AuthRouterRest ( );
        GlobalErrorHandler globalErrorHandler = new GlobalErrorHandler (objectMapper);

        webTestClient = WebTestClient
                .bindToRouterFunction(authRouterRest.authRouterFunction(authHandler))
                .handlerStrategies( HandlerStrategies.builder()
                        .exceptionHandler(globalErrorHandler)
                        .build())
                .build();
    }

    private AuthRequestDTO buildRequest() {

        AuthRequestDTO req = new AuthRequestDTO ( );
        req.setEmail ( "email" );
        req.setPassword ( "password" );

        return req;
    }

    private Auth buildModelFromReq(AuthRequestDTO req) {
        UUID idRolUser = UUID.fromString ( "64d07d7e-6a65-413a-aeb6-674de1f42545" );
        return Auth.builder ( )
                .idUser ( idRolUser )
                .idRol ( RolEnum.ADMIN.getId ( ) )
                .name ( "axalpusa" )
                .token ( "" )
                .build ( );
    }

    @Test
    @DisplayName("POST /api/v1/login - successful")
    void loginCorrect() {
        AuthRequestDTO req = buildRequest ( );
        Auth toSave = buildModelFromReq ( req );
        Auth saved = toSave.toBuilder ( ).build ( );

        AuthResponseDTO response = new AuthResponseDTO ( );
        response.setIdUser ( saved.getIdUser ( ) );
        response.setIdRol ( saved.getIdRol ( ) );
        response.setName ( saved.getName ( ) );
        response.setToken ( saved.getToken ( ) );

        when ( authUseCase.login (
                eq ( req.getEmail ( ) ),
                eq ( req.getPassword ( ) ),
                any ( BiFunction.class ),
                any ( BiFunction.class )
        ) ).thenReturn ( Mono.just ( saved ) );
        when ( transactionalAdapter.executeInTransaction ( any ( Mono.class ) ) )
                .thenAnswer ( invocation -> invocation. < Mono < ? > >getArgument ( 0 ) );

        webTestClient.post ( )
                .uri ( ApiPaths.LOGIN )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( req )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( AuthResponseDTO.class )
                .value ( res -> {
                    assertNotNull ( res.getToken ( ) );
                    assertEquals ( saved.getIdUser ( ), res.getIdUser ( ) );
                    assertEquals ( saved.getIdRol ( ), res.getIdRol ( ) );
                } );
    }


    @Test
    @DisplayName("POST /api/v1/login - no authorized")
    void loginError() {
        when ( authUseCase.login (
                eq ( "axalpusa@gmail.com" ),
                eq ( "axalpusa" ),
                any ( ),
                any ( )
        ) ).thenReturn ( Mono.error ( new UnauthorizedException ( "Invalid credentials" ) ) );

        when ( transactionalAdapter.executeInTransaction ( any ( Mono.class ) ) )
                .thenAnswer ( invocation -> invocation. < Mono < ? > >getArgument ( 0 ) );

        webTestClient.post()
                .uri(ApiPaths.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AuthRequestDTO("axalpusa@gmail.com", "axalpusa"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unauthorized")
                .jsonPath("$.details").isEqualTo("Invalid credentials");
    }

    @Test
    void loginUnauthorizedWithBlock() {
        when(authUseCase.login(
                eq("axalpusa@gmail.com"),
                eq("axalpusa"),
                any(),
                any()
        )).thenReturn(Mono.error(new UnauthorizedException("Invalid credentials")));

        assertThrows(
                UnauthorizedException.class,
                () -> authUseCase.login(
                        "axalpusa@gmail.com",
                        "axalpusa",
                        (idUser, idRol) -> "fakeToken",
                        (raw, encoded) -> false
                ).block()
        );

    }


}