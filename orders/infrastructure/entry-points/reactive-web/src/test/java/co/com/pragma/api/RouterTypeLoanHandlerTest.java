package co.com.pragma.api;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.config.GlobalErrorHandler;
import co.com.pragma.api.dto.request.TypeLoanRequestDTO;
import co.com.pragma.api.dto.response.TypeLoanResponseDTO;
import co.com.pragma.api.handler.TypeLoanHandler;
import co.com.pragma.api.mapper.TypeLoanMapperDTO;
import co.com.pragma.api.routerrest.TypeLoanRouterRest;
import co.com.pragma.api.services.AuthServiceClient;
import co.com.pragma.model.typeloan.TypeLoan;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.typeloan.TypeLoanUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RouterTypeLoanHandlerTest {
    private WebTestClient webTestClient;

    private TypeLoanUseCase typeLoanUseCase;
    private TypeLoanMapperDTO typeLoanMapper;

    private ObjectMapper objectMapper;

    private AuthServiceClient authServiceClient;
    private TransactionalAdapter transactionalAdapter;

    @BeforeEach
    void setup() {
        typeLoanUseCase = mock ( TypeLoanUseCase.class );
        typeLoanMapper = mock ( TypeLoanMapperDTO.class );
        objectMapper = mock ( ObjectMapper.class );
        transactionalAdapter = mock ( TransactionalAdapter.class );
        authServiceClient = mock ( AuthServiceClient.class );
        TypeLoanHandler typeLoanHandler = new TypeLoanHandler ( typeLoanUseCase, objectMapper, typeLoanMapper, transactionalAdapter );

        TypeLoanRouterRest typeLoanRouterRest = new TypeLoanRouterRest ( );
        objectMapper = new ObjectMapper ( );
        GlobalErrorHandler globalErrorHandler = new GlobalErrorHandler ( objectMapper );

        webTestClient = WebTestClient
                .bindToRouterFunction ( typeLoanRouterRest.typeLoanRoutes ( typeLoanHandler ) )
                .handlerStrategies ( HandlerStrategies.builder ( )
                        .exceptionHandler ( globalErrorHandler )
                        .build ( ) )
                .build ( );
    }

    private TypeLoanRequestDTO buildRequest() {

        TypeLoanRequestDTO req = new TypeLoanRequestDTO ( );
        req.setName ( "name" );
        req.setAutomaticValidation ( true );
        req.setInterestRate ( BigDecimal.valueOf ( 0.5 ) );
        req.setMaximumAmount ( BigDecimal.valueOf ( 5000.00 ) );
        req.setMinimumAmount ( BigDecimal.valueOf ( 100.00 ) );

        return req;
    }

    private TypeLoan buildModelFromReq(TypeLoanRequestDTO req) {
        UUID idNewTypeLoan = UUID.randomUUID ( );
        return TypeLoan.builder ( )
                .idTypeLoan ( idNewTypeLoan )
                .name ( "name" )
                .automaticValidation ( true )
                .interestRate ( BigDecimal.valueOf ( 0.5 ) )
                .maximumAmount ( BigDecimal.valueOf ( 5000.00 ) )
                .minimumAmount ( BigDecimal.valueOf ( 100.00 ) )
                .build ( );
    }

    @Test
    @DisplayName("POST /api/v1/typeLoan - successful")
    void saveTypeLoanCorrect() {
        TypeLoanRequestDTO req = buildRequest ( );
        TypeLoan toSave = buildModelFromReq ( req );
        TypeLoan saved = toSave.toBuilder ( ).build ( );

        TypeLoanResponseDTO response = new TypeLoanResponseDTO ( );
        response.setIdTypeLoan ( saved.getIdTypeLoan ( ) );
        response.setAutomaticValidation ( true );
        response.setInterestRate ( BigDecimal.valueOf ( 0.5 ) );
        response.setMaximumAmount ( BigDecimal.valueOf ( 5000.00 ) );
        response.setMinimumAmount ( BigDecimal.valueOf ( 100.00 ) );
        response.setName ( "name" );


        lenient ( ).when ( typeLoanMapper.toModel ( any ( TypeLoanRequestDTO.class ) ) ).thenReturn ( toSave );
        lenient ( ).when ( typeLoanUseCase.saveTypeLoan ( any ( TypeLoan.class ) ) ).thenReturn ( Mono.just ( saved ) );
        lenient ( ).when ( typeLoanMapper.toResponse ( any ( TypeLoan.class ) ) ).thenReturn ( response );
        lenient ( ).when ( transactionalAdapter.executeInTransaction ( any ( Mono.class ) ) )
                .thenAnswer ( invocation -> invocation. < Mono < ? > >getArgument ( 0 ) );

        webTestClient.post ( )
                .uri ( ApiPaths.TYPELOAN )
                .contentType ( MediaType.APPLICATION_JSON )
                .header ( "Authorization", "Bearer mock-token" )
                .bodyValue ( req )
                .exchange ( )
                .expectStatus ( ).isCreated ( )
                .expectBody ( TypeLoanResponseDTO.class )
                .consumeWith ( res -> {
                    TypeLoanResponseDTO body = res.getResponseBody ( );
                    assertThat ( body ).isNotNull ( );
                    assertThat ( body.getName ( ) ).isEqualTo ( "name" );
                } );
    }

    @Test
    @DisplayName("GET /api/v1/typeLoan/{id} - found")
    void getTypeLoanById() {
        TypeLoanRequestDTO req = buildRequest ( );
        TypeLoan model = buildModelFromReq ( req );

        when ( typeLoanUseCase.getTypeLoanById ( model.getIdTypeLoan ( ) ) )
                .thenReturn ( Mono.just ( model ) );

        webTestClient.get ( )
                .uri ( "/api/v1/typeLoan/{idTypeLoan}", model.getIdTypeLoan ( ) )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( )
                .jsonPath ( "$.idTypeLoan" ).isEqualTo ( model.getIdTypeLoan ( ).toString ( ) )
                .jsonPath ( "$.name" ).isEqualTo ( model.getName ( ) );
    }

    @Test
    void testUpdateTypeLoan() {
        UUID uuid = UUID.randomUUID ( );
        TypeLoanResponseDTO dto = new TypeLoanResponseDTO ( );
        dto.setIdTypeLoan ( uuid );
        dto.setName ( "name" );

        TypeLoan existingTypeLoan = new TypeLoan ( );
        existingTypeLoan.setIdTypeLoan ( uuid );
        existingTypeLoan.setName ( "name" );
        existingTypeLoan.setAutomaticValidation ( true );
        existingTypeLoan.setInterestRate ( BigDecimal.valueOf ( 0.5 ) );
        existingTypeLoan.setMaximumAmount ( BigDecimal.valueOf ( 5000.00 ) );
        existingTypeLoan.setMinimumAmount ( BigDecimal.valueOf ( 100.00 ) );

        TypeLoan updatedTypeLoan = new TypeLoan ( );
        updatedTypeLoan.setIdTypeLoan ( uuid );
        updatedTypeLoan.setName ( "name1" );
        updatedTypeLoan.setAutomaticValidation ( true );
        updatedTypeLoan.setInterestRate ( BigDecimal.valueOf ( 0.5 ) );
        updatedTypeLoan.setMaximumAmount ( BigDecimal.valueOf ( 5000.00 ) );
        updatedTypeLoan.setMinimumAmount ( BigDecimal.valueOf ( 100.00 ) );

        when ( typeLoanUseCase.getTypeLoanById ( uuid ) ).thenReturn ( Mono.just ( existingTypeLoan ) );
        when ( typeLoanUseCase.updateTypeLoan ( any ( TypeLoan.class ) ) ).thenReturn ( Mono.just ( updatedTypeLoan ) );

        webTestClient.put ( )
                .uri ( "/api/v1/typeLoan" )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( dto )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( )
                .returnResult ( )
                .getResponseBody ( );

    }

    @Test
    void testDeleteTypeLoanSuccess() {
        UUID typeLoanId = UUID.randomUUID ( );

        when ( typeLoanUseCase.deleteTypeLoanById ( typeLoanId ) ).thenReturn ( Mono.empty ( ) );

        webTestClient.delete ( )
                .uri ( "/api/v1/typeLoan/{idTypeLoan}", typeLoanId )
                .exchange ( )
                .expectStatus ( ).isNoContent ( );
    }

    @Test
    void testDeleteTypeLoanEmptyId() {
        webTestClient.delete ( )
                .uri ( "/api/v1/typeLoan/{idTypeLoan}", "" )
                .exchange ( )
                .expectStatus ( ).isNotFound ( );
    }
}
