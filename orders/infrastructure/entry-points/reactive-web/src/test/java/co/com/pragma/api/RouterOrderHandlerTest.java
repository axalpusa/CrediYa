package co.com.pragma.api;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.config.GlobalErrorHandler;
import co.com.pragma.api.dto.request.OrderRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.api.dto.response.OrderResponseDTO;
import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.enums.StatusEnum;
import co.com.pragma.api.enums.TypeLoanEnum;
import co.com.pragma.api.handler.OrderHandler;
import co.com.pragma.api.mapper.OrderMapperDTO;
import co.com.pragma.api.routerrest.OrderRouterRest;
import co.com.pragma.api.services.AuthServiceClient;
import co.com.pragma.model.order.Order;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.order.OrderUseCase;
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
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RouterOrderHandlerTest {
    private WebTestClient webTestClient;

    private OrderUseCase orderUseCase;

    private OrderMapperDTO orderMapper;

    private ObjectMapper objectMapper;

    private AuthServiceClient authServiceClient;
    private TransactionalAdapter transactionalAdapter;

    @BeforeEach
    void setup() {
        orderUseCase = mock ( OrderUseCase.class );
        orderMapper = mock ( OrderMapperDTO.class );
        objectMapper = mock ( ObjectMapper.class );
        transactionalAdapter = mock ( TransactionalAdapter.class );
        authServiceClient = mock ( AuthServiceClient.class );
        OrderHandler orderHandler = new OrderHandler ( orderUseCase, objectMapper, orderMapper, authServiceClient, transactionalAdapter );
        OrderRouterRest orderRouterRest = new OrderRouterRest ( );
        objectMapper = new ObjectMapper ( );
        GlobalErrorHandler globalErrorHandler = new GlobalErrorHandler ( objectMapper );

        webTestClient = WebTestClient
                .bindToRouterFunction ( orderRouterRest.orderRoutes ( orderHandler ) )
                .handlerStrategies ( HandlerStrategies.builder ( )
                        .exceptionHandler ( globalErrorHandler )
                        .build ( ) )
                .build ( );
    }

    private OrderRequestDTO buildRequest() {

        OrderRequestDTO req = new OrderRequestDTO ( );
        req.setEmailAddress ( "axalpusa1125@gmail.com" );
        req.setTermMonths ( 12 );
        req.setAmount ( new BigDecimal ( "1000" ) );
        req.setDocumentId ( "48295730" );
        req.setIdTypeLoan ( TypeLoanEnum.TYPE2.getId ( ) );
        req.setIdStatus ( StatusEnum.PENDENT.getId ( ) );

        return req;
    }

    private Order buildModelFromReq(OrderRequestDTO req) {
        UUID idNewOrder = UUID.randomUUID ( );
        return Order.builder ( )
                .idOrder ( idNewOrder )
                .emailAddress ( "axalpusa@gmail.com" )
                .amount ( new BigDecimal ( "1000" ) )
                .documentId ( "48295730" )
                .idTypeLoan ( TypeLoanEnum.TYPE2.getId ( ) )
                .idStatus ( StatusEnum.PENDENT.getId ( ) )
                .termMonths ( 12 )
                .build ( );
    }

    @Test
    @DisplayName("POST /api/v1/order - successful")
    void saveOrderCorrect() {
        OrderRequestDTO req = buildRequest();
        Order toSave = buildModelFromReq(req);
        Order saved = toSave.toBuilder().build();

        OrderResponseDTO response = new OrderResponseDTO();
        response.setIdOrder(saved.getIdOrder());
        response.setEmailAddress("axalpusa@gmail.com");
        response.setTermMonths(12);
        response.setAmount(new BigDecimal("1000"));
        response.setDocumentId("48295730");
        response.setIdTypeLoan(TypeLoanEnum.TYPE2.getId());
        response.setIdStatus(StatusEnum.PENDENT.getId());

        AuthResponseDTO dummyAuth = new AuthResponseDTO(
                UUID.randomUUID(),
                RolEnum.CLIENT.getId ( ),
                "axalpusa",
                "token"
        );

        record DummyUser(UUID idUser, UUID idRol, String name, String token) {}
        DummyUser authUser = new DummyUser(
                dummyAuth.getIdUser (),
                dummyAuth.getIdRol (),
                dummyAuth.getName (),
                dummyAuth.getToken ()
        );

        when(authServiceClient.validateToken(anyString())).thenReturn(Mono.just(dummyAuth));

        lenient().when(orderMapper.toModel(any(OrderRequestDTO.class))).thenReturn(toSave);
        lenient().when(orderUseCase.saveOrder(any(Order.class))).thenReturn(Mono.just(saved));
        lenient().when(orderMapper.toResponse(any(Order.class))).thenReturn(response);
        lenient().when(transactionalAdapter.executeInTransaction(any(Mono.class)))
                .thenAnswer(invocation -> invocation.<Mono<?>>getArgument(0));

        webTestClient.post()
                .uri(ApiPaths.ORDER)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock-token")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseDTO.class)
                .consumeWith(res -> {
                    OrderResponseDTO body = res.getResponseBody();
                    assertThat(body).isNotNull();
                    assertThat(body.getEmailAddress()).isEqualTo("axalpusa@gmail.com");
                    assertThat(body.getIdOrder()).isEqualTo(saved.getIdOrder());
                    assertThat(body.getTermMonths()).isEqualTo(12);
                    assertThat(body.getAmount()).isEqualByComparingTo(new BigDecimal("1000"));
                });
    }

    @Test
    @DisplayName("GET /api/v1/order/{id} - found")
    void getOrderById() {
        OrderRequestDTO req = buildRequest ( );
        Order model = buildModelFromReq ( req );

        when ( orderUseCase.getOrderById ( model.getIdOrder ( ) ) )
                .thenReturn ( Mono.just ( model ) );

        webTestClient.get ( )
                .uri ( "/api/v1/order/{idOrder}", model.getIdOrder ( ) )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( )
                .jsonPath ( "$.idOrder" ).isEqualTo ( model.getIdOrder ( ).toString ( ) )
                .jsonPath ( "$.emailAddress" ).isEqualTo ( model.getEmailAddress ( ) );
    }

    @Test
    void testUpdateOrder() {
        UUID uuid = UUID.randomUUID ( );
        OrderResponseDTO dto = new OrderResponseDTO ( );
        dto.setIdOrder ( uuid );
        dto.setEmailAddress ( "axalpusa@gmail.com" );

        Order existingOrder = new Order ( );
        existingOrder.setIdOrder ( uuid );
        existingOrder.setEmailAddress ( "axalpusa1125@gmail.com" );
        existingOrder.setTermMonths ( 12 );
        existingOrder.setAmount ( new BigDecimal ( "1000" ) );
        existingOrder.setDocumentId ( "48295730" );
        existingOrder.setIdTypeLoan ( TypeLoanEnum.TYPE2.getId ( ) );
        existingOrder.setIdStatus ( StatusEnum.PENDENT.getId ( ) );

        Order updatedOrder = new Order ( );
        updatedOrder.setIdOrder ( uuid );
        updatedOrder.setEmailAddress ( "axalpusa@gmail.com" );
        updatedOrder.setTermMonths ( 12 );
        updatedOrder.setAmount ( new BigDecimal ( "1000" ) );
        updatedOrder.setDocumentId ( "48295730" );
        updatedOrder.setIdTypeLoan ( TypeLoanEnum.TYPE2.getId ( ) );
        updatedOrder.setIdStatus ( StatusEnum.PENDENT.getId ( ) );

        when ( orderUseCase.getOrderById ( uuid ) ).thenReturn ( Mono.just ( existingOrder ) );
        when ( orderUseCase.updateOrder ( any ( Order.class ) ) ).thenReturn ( Mono.just ( updatedOrder ) );

        webTestClient.put ( )
                .uri ( "/api/v1/order" )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( dto )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( )
                .returnResult ( )
                .getResponseBody ( );

    }

    @Test
    void testDeleteOrderSuccess() {
        UUID orderId = UUID.randomUUID ( );

        when ( orderUseCase.deleteOrderById ( orderId ) ).thenReturn ( Mono.empty ( ) );

        webTestClient.delete ( )
                .uri ( "/api/v1/order/{idOrder}", orderId )
                .exchange ( )
                .expectStatus ( ).isNoContent ( );
    }

    @Test
    void testDeleteOrderEmptyId() {
        webTestClient.delete ( )
                .uri ( "/api/v1/order/{idOrder}", "" )
                .exchange ( )
                .expectStatus ( ).isNotFound ( );
    }
}
