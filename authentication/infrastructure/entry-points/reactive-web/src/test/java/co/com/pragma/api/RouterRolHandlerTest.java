package co.com.pragma.api;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.config.GlobalErrorHandler;
import co.com.pragma.api.dto.request.RolRequestDTO;
import co.com.pragma.api.dto.response.RolResponseDTO;
import co.com.pragma.api.handler.RolHandler;
import co.com.pragma.api.mapper.RolMapperDTO;
import co.com.pragma.api.routerrest.RolRouterRest;
import co.com.pragma.model.rol.Rol;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.rol.RolUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouterRolHandlerTest {

    private WebTestClient webTestClient;

    private RolUseCase rolUseCase;

    private RolMapperDTO rolMapper;

    private ObjectMapper objectMapper;

    private TransactionalAdapter transactionalAdapter;

    @BeforeEach
    void setup() {
        rolUseCase = mock ( RolUseCase.class );
        rolMapper = mock ( RolMapperDTO.class );
        objectMapper = mock ( ObjectMapper.class );
        transactionalAdapter = mock ( TransactionalAdapter.class );
        RolHandler rolHandler = new RolHandler ( rolUseCase, objectMapper, rolMapper, transactionalAdapter );
        RolRouterRest rolRouterRest = new RolRouterRest ( );
        objectMapper = new ObjectMapper ( );
        GlobalErrorHandler globalErrorHandler = new GlobalErrorHandler ( objectMapper );

        webTestClient = WebTestClient
                .bindToRouterFunction ( rolRouterRest.rolRouterFunction ( rolHandler ) )
                .handlerStrategies ( HandlerStrategies.builder ( )
                        .exceptionHandler ( globalErrorHandler )
                        .build ( ) )
                .build ( );
    }

    private RolRequestDTO buildRequest() {

        RolRequestDTO req = new RolRequestDTO ( );
        req.setName ( "user" );
        req.setDescription ( "description" );

        return req;
    }

    private Rol buildModelFromReq(RolRequestDTO req) {
        UUID idNewRol = UUID.randomUUID ( );
        return Rol.builder ( )
                .idRol ( idNewRol )
                .name ( req.getName ( ) )
                .description ( req.getDescription ( ) )
                .build ( );
    }

    @Test
    @DisplayName("POST /api/v1/rol - successful")
    void saveRolCorrect() {
        RolRequestDTO req = buildRequest ( );
        Rol toSave = buildModelFromReq ( req );
        Rol saved = toSave.toBuilder ( ).build ( );
        RolResponseDTO response = new RolResponseDTO ( );
        response.setIdRol ( saved.getIdRol ( ) );
        response.setName ( saved.getName ( ) );
        response.setDescription ( saved.getDescription ( ) );

        lenient ( ).when ( rolMapper.toModel ( any ( RolRequestDTO.class ) ) ).thenReturn ( toSave );
        lenient ( ).when ( rolUseCase.saveRol ( any ( Rol.class ) ) ).thenReturn ( Mono.just ( saved ) );
        lenient ( ).when ( rolMapper.toResponse ( any ( Rol.class ) ) ).thenReturn ( response );
        lenient ( ).when ( transactionalAdapter.executeInTransaction ( any ( Mono.class ) ) )
                .thenAnswer ( invocation -> invocation. < Mono < ? > >getArgument ( 0 ) );
        webTestClient.post ( )
                .uri ( ApiPaths.ROL )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( req )
                .exchange ( )
                .expectStatus ( ).isCreated ( )
                .expectBody ( )
        ;

    }

    @Test
    @DisplayName("POST /api/v1/rol - validation_error")
    void saveRolValidationError() {
        RolRequestDTO req = buildRequest ( );
        req.setName ( "" );
        Rol invalidRol = buildModelFromReq ( req );
        when ( rolMapper.toModel ( any ( RolRequestDTO.class ) ) ).thenReturn ( invalidRol );
        when ( rolUseCase.saveRol ( any ( Rol.class ) ) )
                .thenReturn ( Mono.error ( new ValidationException ( List.of ( "Name is required." ) ) ) );
        when ( transactionalAdapter.executeInTransaction ( any ( Mono.class ) ) )
                .thenAnswer ( invocation -> invocation. < Mono < ? > >getArgument ( 0 ) );
        webTestClient.post ( )
                .uri ( ApiPaths.ROL )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( req )
                .exchange ( )
                .expectStatus ( ).isBadRequest ( )
                .expectBody ( )
                .jsonPath ( "$.details" ).isEqualTo ( "Name is required." );
    }

    @Test
    @DisplayName("GET /api/v1/rol/{id} - found")
    void getRolById() {
        RolRequestDTO req = buildRequest ( );
        Rol model = buildModelFromReq ( req );

        when ( rolUseCase.getRolById ( model.getIdRol ( ) ) )
                .thenReturn ( Mono.just ( model ) );

        webTestClient.get ( )
                .uri ( "/api/v1/rol/{idRol}", model.getIdRol ( ) )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( )
                .jsonPath ( "$.idRol" ).isEqualTo ( model.getIdRol ( ).toString ( ) )
                .jsonPath ( "$.name" ).isEqualTo ( model.getName ( ) );
    }

    @Test
    void testUpdateRol() {
        UUID uuid = UUID.randomUUID ( );
        RolResponseDTO dto = new RolResponseDTO ( );
        dto.setIdRol ( uuid );
        dto.setName ( "Nuevo nombre" );

        Rol existingRol = new Rol ( );
        existingRol.setIdRol ( uuid );
        existingRol.setName ( "Nombre viejo" );
        existingRol.setDescription ( "Descripcion vieja" );

        Rol updatedRol = new Rol ( );
        updatedRol.setIdRol ( uuid );
        updatedRol.setName ( "Nuevo nombre" );
        updatedRol.setDescription ( "Descripcion vieja" );

        when ( rolUseCase.getRolById ( uuid ) ).thenReturn ( Mono.just ( existingRol ) );
        when ( rolUseCase.updateRol ( any ( Rol.class ) ) ).thenReturn ( Mono.just ( updatedRol ) );

        webTestClient.put ( )
                .uri ( "/api/v1/rol" )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( dto )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( )
                .returnResult ( )
                .getResponseBody ( );

    }

    @Test
    void testDeleteRolSuccess() {
        UUID rolId = UUID.randomUUID ( );

        when ( rolUseCase.deleteRolById ( rolId ) ).thenReturn ( Mono.empty ( ) );

        webTestClient.delete ( )
                .uri ( "/api/v1/rol/{idRol}", rolId )
                .exchange ( )
                .expectStatus ( ).isNoContent ( );
    }

    @Test
    void testDeleteRolEmptyId() {
        webTestClient.delete ( )
                .uri ( "/api/v1/ol/{idRol}", "" )
                .exchange ( )
                .expectStatus ( ).isNotFound ( );
    }

}