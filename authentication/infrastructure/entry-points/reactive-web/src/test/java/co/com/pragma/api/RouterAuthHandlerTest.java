package co.com.pragma.api;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.AuthRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.handler.AuthHandler;
import co.com.pragma.api.jwt.JwtService;
import co.com.pragma.api.routerrest.AuthRouterRest;
import co.com.pragma.model.auth.Auth;
import co.com.pragma.usecase.authentication.AuthUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @BeforeEach
    void setup() {
        authUseCase = mock ( AuthUseCase.class );
        jwtService = mock ( JwtService.class );
        passwordEncoder = mock ( PasswordEncoder.class );
        //  authMapper = mock(AuthMapperDTO.class);

        AuthHandler authHandler = new AuthHandler ( authUseCase, jwtService, passwordEncoder );
        //ReflectionTestUtils.setField(authHandler, "authMapper", authMapper);
        AuthRouterRest authRouterRest = new AuthRouterRest ( );

        webTestClient = WebTestClient.bindToRouterFunction (
                authRouterRest.authRouterFunction ( authHandler )
        ).build ( );
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

        // when(authMapper.toModel(any(AuthRequestDTO.class))).thenReturn(toSave);
        when ( authUseCase.login (
                eq ( req.getEmail ( ) ),
                eq ( req.getPassword ( ) ),
                any ( BiFunction.class ),
                any ( BiFunction.class )
        ) ).thenReturn ( Mono.just ( saved ) );
        // when(authMapper.toResponse(any(Auth.class))).thenReturn(response);

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
        ) ).thenReturn ( Mono.error ( new RuntimeException ( "Invalid credentials" ) ) );

        webTestClient.post ( )
                .uri ( ApiPaths.LOGIN )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( new AuthRequestDTO ( "axalpusa@gmail.com", "axalpusa" ) )
                .exchange ( )
                .expectStatus ( ).isUnauthorized ( )
                .expectHeader ( ).contentType ( MediaType.APPLICATION_JSON )
                .expectBody ( Map.class )
                .value ( body -> {
                    assert body.get ( "error" ).equals ( "Invalid credentials" );
                } );
    }


}