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
import co.com.pragma.usecase.order.OrderUseCase;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
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

    @TestConfiguration
    static class MockBeans {

        @Bean
        OrderUseCase orderUseCase() {
            return mock ( OrderUseCase.class );
        }
        @Bean
        AuthServiceClient authServiceClient() {
            return mock ( AuthServiceClient.class );
        }
        @Bean
        OrderMapperDTO orderDTOMapper() {
            return mock ( OrderMapperDTO.class );
        }
    }
    private OrderRequestDTO buildOrderRequest () {
        OrderRequestDTO req = new OrderRequestDTO ( );
        req.setEmailAddress ( "axalpusa1125@gmail.com" );
        req.setTermMonths ( 12 );
        req.setAmount ( new BigDecimal ( "1000" ) );
        req.setDocumentId ( "48295730" );
        req.setIdStatus ( StatusEnum.PENDENT.getId ( ) );
        req.setIdTypeLoan ( TypeLoanEnum.TYPE2.getId ( ) );

        return req;
    }
    @Autowired
    private AuthServiceClient authServiceClient;

    @Test
    void securityHeadersShouldBePresent() {
        when(authServiceClient.validateToken(anyString()))
                .thenReturn(Mono.just(AuthResponseDTO.builder()
                        .idUser(UUID.randomUUID())
                        .idRol(RolEnum.CLIENT.getId())
                        .token("Bearer faketoken123")
                        .build()));

        webTestClient.post()
                .uri(ApiPaths.ORDER)
                .header("Authorization", "Bearer faketoken123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildOrderRequest())
                .exchange()
                .expectStatus().isOk()
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