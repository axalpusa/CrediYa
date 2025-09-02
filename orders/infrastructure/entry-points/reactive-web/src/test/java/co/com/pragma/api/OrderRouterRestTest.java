package co.com.pragma.api;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.OrderRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.api.dto.response.OrderResponseDTO;
import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.enums.TypeLoanEnum;
import co.com.pragma.api.handler.OrderHandler;
import co.com.pragma.api.mapper.OrderMapperDTO;
import co.com.pragma.api.routerrest.OrderRouterRest;
import co.com.pragma.api.services.AuthServiceClient;
import co.com.pragma.model.order.Order;
import co.com.pragma.usecase.order.OrderUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.test.web.reactive.server.HttpHandlerConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
        orderUseCase = mock(OrderUseCase.class);
        validator = mock(jakarta.validation.Validator.class);
        orderMapper = mock(OrderMapperDTO.class);
        objectMapper = mock(ObjectMapper.class);
        authServiceClient = mock(AuthServiceClient.class);
        UUID rolClient = RolEnum.CLIENT.getId();
        AuthResponseDTO response = AuthResponseDTO.builder()
                .idUser(UUID.randomUUID())
                .idRol(rolClient)
                .token("faketoken123")
                .build();

        lenient().when(authServiceClient.validateToken(anyString()))
                .thenReturn(Mono.just(response));

        OrderHandler handler = new OrderHandler(orderUseCase, objectMapper, orderMapper, authServiceClient);
        RouterFunction<ServerResponse> router = new OrderRouterRest().orderRoutes(handler);
        webTestClient = WebTestClient.bindToRouterFunction(router).build();
    }


    @Test
    @DisplayName("POST /api/v1/order - éxito")
    void saveOrderCorrect() {
        OrderRequestDTO req = buildRequest();
        Order toSave = buildModelFromReq(req);
        Order saved = toSave.toBuilder().idOrder(UUID.randomUUID()).build();

        when(orderMapper.toModel(any(OrderRequestDTO.class))).thenReturn(toSave);
        when(orderUseCase.saveOrder(any(Order.class))).thenReturn(Mono.just(saved));

        webTestClient.post()
                .uri(ApiPaths.ORDER)
                .header("Authorization", "Bearer faketoken123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.idOrder").isEqualTo(saved.getIdOrder());
    }

    @Test
    @DisplayName("POST /api/v1/order - usuario no autorizado")
    void saveOrderUnauthorized() {
        UUID rolAdmin = RolEnum.ADMIN.getId();
        AuthResponseDTO response = AuthResponseDTO.builder()
                .idUser(UUID.randomUUID())
                .idRol(rolAdmin)
                .token("faketoken123")
                .build();

        when(authServiceClient.validateToken(anyString()))
                .thenReturn(Mono.just(response));

        OrderRequestDTO req = buildRequest();

        webTestClient.post()
                .uri(ApiPaths.ORDER)
                .header("Authorization", "Bearer faketoken123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("User is not allowed to create orders");
    }


}
