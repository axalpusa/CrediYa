package co.com.pragma.api;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.OrderRequestDTO;
import co.com.pragma.api.dto.request.ReportRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.api.dto.response.UserReportResponseDTO;
import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.enums.TypeLoanEnum;
import co.com.pragma.api.handler.OrderHandler;
import co.com.pragma.api.mapper.OrderMapperDTO;
import co.com.pragma.api.routerrest.OrderRouterRest;
import co.com.pragma.api.services.AuthServiceClient;
import co.com.pragma.model.dto.OrderPendingDTO;
import co.com.pragma.model.order.Order;
import co.com.pragma.usecase.order.OrderUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OrderRouterRestTest {

    private WebTestClient webTestClient;
    private OrderUseCase orderUseCase;
    private Validator validator;
    private OrderMapperDTO orderMapper;

    private ObjectMapper objectMapper;
    private AuthServiceClient authServiceClient;

    private OrderRequestDTO buildRequest() {
        OrderRequestDTO req = new OrderRequestDTO ( );
        req.setAmount ( new BigDecimal ( "1000" ) );
        req.setDocumentId ( "48295730" );
        req.setEmailAddress ( "axalpusa1125@gmail.com" );
        req.setTermMonths ( 12 );
        req.setIdTypeLoan ( TypeLoanEnum.TYPE1.getId ( ) );
        return req;
    }


    private Order buildModelFromReq(OrderRequestDTO req) {
        return Order.builder ( )
                .idOrder ( null )
                .amount ( req.getAmount ( ) )
                .documentId ( req.getDocumentId ( ) )
                .emailAddress ( req.getEmailAddress ( ) )
                .emailAddress ( req.getEmailAddress ( ) )
                .termMonths ( req.getTermMonths ( ) )
                .documentId ( req.getDocumentId ( ) )
                .idTypeLoan ( req.getIdTypeLoan ( ) )
                .build ( );
    }


    @BeforeEach
    void setup() {
        orderUseCase = mock ( OrderUseCase.class );
        validator = mock ( jakarta.validation.Validator.class );
        orderMapper = mock ( OrderMapperDTO.class );
        objectMapper = mock ( ObjectMapper.class );
        authServiceClient = mock ( AuthServiceClient.class );
        UUID rolClient = RolEnum.CLIENT.getId ( );
        AuthResponseDTO response = AuthResponseDTO.builder ( )
                .idUser ( UUID.randomUUID ( ) )
                .idRol ( rolClient )
                .token ( "faketoken123" )
                .build ( );

        lenient ( ).when ( authServiceClient.validateToken ( anyString ( ) ) )
                .thenReturn ( Mono.just ( response ) );

        OrderHandler handler = new OrderHandler ( orderUseCase, objectMapper, orderMapper, authServiceClient );
        RouterFunction < ServerResponse > router = new OrderRouterRest ( ).orderRoutes ( handler );
        webTestClient = WebTestClient.bindToRouterFunction ( router ).build ( );
    }


    @Test
    @DisplayName("POST /api/v1/order - éxito")
    void saveOrderCorrect() {
        OrderRequestDTO req = buildRequest ( );
        Order toSave = buildModelFromReq ( req );
        Order saved = toSave.toBuilder ( ).idOrder ( UUID.randomUUID ( ) ).build ( );

        when ( orderMapper.toModel ( any ( OrderRequestDTO.class ) ) ).thenReturn ( toSave );
        when ( orderUseCase.saveOrder ( any ( Order.class ) ) ).thenReturn ( Mono.just ( saved ) );

        webTestClient.post ( )
                .uri ( ApiPaths.ORDER )
                .header ( "Authorization", "Bearer faketoken123" )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( req )
                .exchange ( )
                .expectStatus ( ).isCreated ( )
                .expectHeader ( ).contentTypeCompatibleWith ( MediaType.APPLICATION_JSON )
                .expectBody ( )
                .jsonPath ( "$.idOrder" ).isEqualTo ( saved.getIdOrder ( ) );
    }

    @Test
    @DisplayName("POST /api/v1/order/report - éxito")
    void reportCorrect() {
        ReportRequestDTO req = ReportRequestDTO.builder ( )
                .status ( UUID.randomUUID ( ) )
                .email ( "test@example.com" )
                .page ( 0 )
                .size ( 10 )
                .build ( );

        AuthResponseDTO mockAuthUser = AuthResponseDTO.builder ( )
                .idRol ( RolEnum.ASSESSOR.getId ( ) )
                .build ( );
        when ( authServiceClient.validateToken ( anyString ( ) ) ).thenReturn ( Mono.just ( mockAuthUser ) );

        OrderPendingDTO order1 = OrderPendingDTO.builder ( )
                .amount ( BigDecimal.valueOf ( 1000 ) )
                .termMonths ( 12 )
                .email ( "test@example.com" )
                .typeLoan ( "Personal" )
                .interestRate ( BigDecimal.valueOf ( 0.05 ) )
                .statusOrder ( "Aprobado" )
                .totalMonthlyDebtApprovedRequests ( BigDecimal.valueOf ( 100 ) )
                .build ( );

        UserReportResponseDTO user = UserReportResponseDTO.builder ( )
                .emailAddress ( "test@example.com" )
                .firstName ( "axel" )
                .lastName ( "Puertas" )
                .baseSalary ( BigDecimal.valueOf ( 2000 ) )
                .build ( );

        when ( orderUseCase.findPendingOrders ( any ( UUID.class ), anyString ( ), anyInt ( ), anyInt ( ) ) )
                .thenReturn ( Flux.just ( order1 ) );
        when ( authServiceClient.getUserByEmailAddress ( any ( ), anyString ( ) ) )
                .thenReturn ( Mono.just ( user ) );


        webTestClient.post ( )
                .uri ( ApiPaths.REPORT )
                .header ( "Authorization", "Bearer faketoken123" )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( req )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectHeader ( ).contentTypeCompatibleWith ( MediaType.APPLICATION_JSON )
                .expectBody ( )
                .jsonPath ( "$[0].email" ).isEqualTo ( "test@example.com" )
                .jsonPath ( "$[0].name" ).isEqualTo ( "axel Puertas" ) // coincide con tu handler
                .jsonPath ( "$[0].amount" ).isEqualTo ( 1000 )
                .jsonPath ( "$[0].statusOrder" ).isEqualTo ( "Aprobado" )
                .jsonPath ( "$[0].totalMonthlyDebtApprovedRequests" ).isEqualTo ( 100 );
    }


}
