package co.com.pragma.api;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.OrderRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.api.dto.response.UserReportResponseDTO;
import co.com.pragma.api.handler.OrderHandler;
import co.com.pragma.api.mapper.OrderMapperDTO;
import co.com.pragma.api.routerrest.OrderRouterRest;
import co.com.pragma.api.services.AuthServiceClient;
import co.com.pragma.model.order.OrderPending;
import co.com.pragma.model.order.Order;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.order.OrderUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.RolEnum;
import enums.TypeLoanEnum;
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
    private TransactionalAdapter transactionalAdapter;
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
        transactionalAdapter = mock ( TransactionalAdapter.class );
        UUID rolClient = RolEnum.CLIENT.getId ( );
        AuthResponseDTO response = AuthResponseDTO.builder ( )
                .idUser ( UUID.randomUUID ( ) )
                .idRol ( rolClient )
                .token ( "faketoken123" )
                .build ( );

        lenient ( ).when ( authServiceClient.validateToken ( anyString ( ) ) )
                .thenReturn ( Mono.just ( response ) );

        OrderHandler handler = new OrderHandler ( orderUseCase, objectMapper, orderMapper, authServiceClient, transactionalAdapter );
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
        when ( transactionalAdapter.executeInTransaction ( any ( Mono.class ) ) )
                .thenAnswer ( invocation -> invocation. < Mono < ? > >getArgument ( 0 ) );

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
    @DisplayName("GET- /api/v1/order/report - éxito")
    void reportCorrectGet() {
        UUID statusId = UUID.randomUUID();
        String email = "axalpusa1125@gmail.com";
        int page = 0;
        int size = 10;

        AuthResponseDTO mockAuthUser = AuthResponseDTO.builder()
                .idRol(RolEnum.ASSESSOR.getId())
                .build();
        lenient().when(authServiceClient.validateToken(anyString())).thenReturn(Mono.just(mockAuthUser));
        lenient().when(transactionalAdapter.executeInTransaction(any(Mono.class)))
                .thenAnswer(invocation -> invocation.<Mono<?>>getArgument(0));
        lenient().when(transactionalAdapter.executeInTransaction(any(Flux.class)))
                .thenAnswer(invocation -> invocation.<Flux<?>>getArgument(0));

        OrderPending order1 = OrderPending.builder()
                .amount(BigDecimal.valueOf(1000))
                .termMonths(12)
                .email("test@example.com")
                .typeLoan("Personal")
                .interestRate(BigDecimal.valueOf(0.05))
                .statusOrder("Aprobado")
                .totalMonthlyDebtApprovedRequests(BigDecimal.valueOf(100))
                .build();

        UserReportResponseDTO user = UserReportResponseDTO.builder()
                .emailAddress("test@example.com")
                .firstName("axel")
                .lastName("Puertas")
                .baseSalary(BigDecimal.valueOf(2000))
                .build();

        when(orderUseCase.findPendingOrders(any(UUID.class),anyString (), anyInt(), anyInt()))
                .thenReturn(Flux.just(order1));
        when(authServiceClient.getUserByEmailAddress(any(), anyString()))
                .thenReturn(Mono.just(user));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.REPORT)
                        .queryParam("status", statusId)
                        .queryParam("email", email)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build()
                )
                .header("Authorization", "Bearer faketoken123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].email").isEqualTo("test@example.com")
                .jsonPath("$[0].name").isEqualTo("axel Puertas")
                .jsonPath("$[0].amount").isEqualTo(1000)
                .jsonPath("$[0].statusOrder").isEqualTo("Aprobado")
                .jsonPath("$[0].totalMonthlyDebtApprovedRequests").isEqualTo(100);
    }


}
