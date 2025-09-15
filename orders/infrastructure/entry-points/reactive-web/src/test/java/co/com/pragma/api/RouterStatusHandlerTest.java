package co.com.pragma.api;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.config.GlobalErrorHandler;
import co.com.pragma.api.dto.request.StatusRequestDTO;
import co.com.pragma.api.dto.response.StatusResponseDTO;
import co.com.pragma.api.handler.StatusHandler;
import co.com.pragma.api.mapper.StatusMapperDTO;
import co.com.pragma.api.routerrest.StatusRouterRest;
import co.com.pragma.api.services.AuthServiceClient;
import co.com.pragma.model.status.Status;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.status.StatusUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RouterStatusHandlerTest {
    private WebTestClient webTestClient;

    private StatusUseCase statusUseCase;
    private StatusMapperDTO statusMapper;

    private ObjectMapper objectMapper;

    private AuthServiceClient authServiceClient;
    private TransactionalAdapter transactionalAdapter;

    @BeforeEach
    void setup() {
        statusUseCase = mock ( StatusUseCase.class );
        statusMapper = mock ( StatusMapperDTO.class );
        objectMapper = mock ( ObjectMapper.class );
        transactionalAdapter = mock ( TransactionalAdapter.class );
        authServiceClient = mock ( AuthServiceClient.class );
        StatusHandler statusHandler = new StatusHandler ( statusUseCase, objectMapper, statusMapper, transactionalAdapter );

        StatusRouterRest statusRouterRest = new StatusRouterRest ( );
        objectMapper = new ObjectMapper ( );
        GlobalErrorHandler globalErrorHandler = new GlobalErrorHandler ( objectMapper );

        webTestClient = WebTestClient
                .bindToRouterFunction ( statusRouterRest.statusRoutes ( statusHandler ) )
                .handlerStrategies ( HandlerStrategies.builder ( )
                        .exceptionHandler ( globalErrorHandler )
                        .build ( ) )
                .build ( );
    }

    private StatusRequestDTO buildRequest() {

        StatusRequestDTO req = new StatusRequestDTO ( );
        req.setDescription ( "desc" );
        req.setName ( "name" );

        return req;
    }

    private Status buildModelFromReq(StatusRequestDTO req) {
        UUID idNewStatus = UUID.randomUUID ( );
        return Status.builder ( )
                .idStatus ( idNewStatus )
                .name ( "name" )
                .description ( "desc" )
                .build ( );
    }

    @Test
    @DisplayName("POST /api/v1/status - successful")
    void saveStatusCorrect() {
        StatusRequestDTO req = buildRequest ( );
        Status toSave = buildModelFromReq ( req );
        Status saved = toSave.toBuilder ( ).build ( );

        StatusResponseDTO response = new StatusResponseDTO ( );
        response.setIdStatus ( saved.getIdStatus ( ) );
        response.setDescription ("desc" );
        response.setName ( "name");


        lenient ( ).when ( statusMapper.toModel ( any ( StatusRequestDTO.class ) ) ).thenReturn ( toSave );
        lenient ( ).when ( statusUseCase.saveStatus ( any ( Status.class ) ) ).thenReturn ( Mono.just ( saved ) );
        lenient ( ).when ( statusMapper.toResponse ( any ( Status.class ) ) ).thenReturn ( response );
        lenient ( ).when ( transactionalAdapter.executeInTransaction ( any ( Mono.class ) ) )
                .thenAnswer ( invocation -> invocation. < Mono < ? > >getArgument ( 0 ) );

        webTestClient.post ( )
                .uri ( ApiPaths.STATUS )
                .contentType ( MediaType.APPLICATION_JSON )
                .header ( "Authorization", "Bearer mock-token" )
                .bodyValue ( req )
                .exchange ( )
                .expectStatus ( ).isCreated ( )
                .expectBody ( StatusResponseDTO.class )
                .consumeWith ( res -> {
                    StatusResponseDTO body = res.getResponseBody ( );
                    assertThat ( body ).isNotNull ( );
                    assertThat ( body.getDescription ( ) ).isEqualTo ( "desc" );
                    assertThat ( body.getName ( ) ).isEqualTo ( "name" );
                } );
    }

    @Test
    @DisplayName("GET /api/v1/status/{id} - found")
    void getStatusById() {
        StatusRequestDTO req = buildRequest ( );
        Status model = buildModelFromReq ( req );

        when ( statusUseCase.getStatusById ( model.getIdStatus ( ) ) )
                .thenReturn ( Mono.just ( model ) );

        webTestClient.get ( )
                .uri ( "/api/v1/status/{idStatus}", model.getIdStatus ( ) )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( )
                .jsonPath ( "$.idStatus" ).isEqualTo ( model.getIdStatus ( ).toString ( ) )
                .jsonPath ( "$.name" ).isEqualTo ( model.getName ( ) );
    }

    @Test
    void testUpdateStatus() {
        UUID uuid = UUID.randomUUID ( );
        StatusResponseDTO dto = new StatusResponseDTO ( );
        dto.setIdStatus ( uuid );
        dto.setName ( "name" );

        Status existingStatus = new Status ( );
        existingStatus.setIdStatus ( uuid );
        existingStatus.setName ( "name" );
        existingStatus.setDescription ( "desc" );

        Status updatedStatus = new Status ( );
        updatedStatus.setIdStatus ( uuid );
        updatedStatus.setDescription ( "desc" );
        updatedStatus.setName ( "name1" );
        updatedStatus.setIdStatus ( StatusEnum.PENDENT.getId ( ) );

        when ( statusUseCase.getStatusById ( uuid ) ).thenReturn ( Mono.just ( existingStatus ) );
        when ( statusUseCase.updateStatus ( any ( Status.class ) ) ).thenReturn ( Mono.just ( updatedStatus ) );

        webTestClient.put ( )
                .uri ( "/api/v1/status" )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( dto )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( )
                .returnResult ( )
                .getResponseBody ( );

    }

    @Test
    void testDeleteStatusSuccess() {
        UUID statusId = UUID.randomUUID ( );

        when ( statusUseCase.deleteStatusById ( statusId ) ).thenReturn ( Mono.empty ( ) );

        webTestClient.delete ( )
                .uri ( "/api/v1/status/{idStatus}", statusId )
                .exchange ( )
                .expectStatus ( ).isNoContent ( );
    }

    @Test
    void testDeleteStatusEmptyId() {
        webTestClient.delete ( )
                .uri ( "/api/v1/status/{idStatus}", "" )
                .exchange ( )
                .expectStatus ( ).isNotFound ( );
    }
}
