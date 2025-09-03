package co.com.pragma.api.config;

import co.com.pragma.api.dto.request.OrderRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
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
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {OrderRouterRest.class, OrderHandler.class})
@WebFluxTest
@Import({
        OrderRouterRest.class,
        OrderHandler.class,
        CorsConfig.class,
        SecurityHeadersConfig.class,
        ConfigTest.MockBeans.class
})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private OrderMapperDTO orderMapper;

    @Autowired
    private OrderUseCase orderUseCase;

    @Autowired
    private TransactionalAdapter transactionalAdapter;

    @TestConfiguration
    static class MockBeans {

        @Bean
        OrderUseCase orderUseCase() {
            return mock(OrderUseCase.class);
        }

        @Bean
        AuthServiceClient authServiceClient() {
            return mock(AuthServiceClient.class);
        }

        @Bean
        OrderMapperDTO orderDTOMapper() {
            return mock(OrderMapperDTO.class);
        }

        @Bean
        TransactionalAdapter transactionalAdapter() {
            return mock(TransactionalAdapter.class);
        }

    }

    private OrderRequestDTO buildOrderRequest() {
        when(orderMapper.toModel(any(OrderRequestDTO.class)))
                .thenAnswer(invocation -> {
                    OrderRequestDTO dto = invocation.getArgument(0);
                    Order order = new Order();
                    order.setAmount(dto.getAmount());
                    order.setTermMonths(dto.getTermMonths());
                    order.setDocumentId(dto.getDocumentId());
                    order.setEmailAddress(dto.getEmailAddress());
                    order.setIdTypeLoan(dto.getIdTypeLoan());
                    order.setIdStatus(StatusEnum.PENDENT.getId());
                    return order;
                });

        when(orderUseCase.saveOrder(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setIdOrder(UUID.randomUUID());
                    return Mono.just(order);
                });

        OrderRequestDTO req = new OrderRequestDTO();
        req.setEmailAddress("axalpusa1125@gmail.com");
        req.setTermMonths(12);
        req.setAmount(new BigDecimal("1000"));
        req.setDocumentId("48295730");
        req.setIdTypeLoan(TypeLoanEnum.TYPE2.getId());
        return req;
    }

    @Test
    void securityHeadersShouldBePresent() {
        when(authServiceClient.validateToken(anyString()))
                .thenReturn(Mono.just(AuthResponseDTO.builder()
                        .idUser(UUID.randomUUID())
                        .idRol(RolEnum.CLIENT.getId())
                        .name("axel puertas")
                        .token("Bearer faketoken123")
                        .build()));
        when(transactionalAdapter.executeInTransaction(any(Mono.class)))
                .thenAnswer(invocation -> invocation.<Mono<?>>getArgument(0));

        webTestClient.post()
                .uri(ApiPaths.ORDER)
                .header("Authorization", "Bearer faketoken123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildOrderRequest())
                .exchange()
                .expectStatus().isCreated ()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().value("Strict-Transport-Security", v -> assertTrue(v.startsWith("max-age=31536000")))
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().value("Cache-Control", v -> assertTrue(v.contains("no-store")))
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin")
                .expectHeader().valueEquals("X-Frame-Options", "DENY");
    }

}