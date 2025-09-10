package co.com.pragma.api;

import co.com.pragma.api.openapi.OrderOpenApi;
import co.com.pragma.api.openapi.StatusOpenApi;
import co.com.pragma.api.openapi.TypeLoanOpenApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiTest {

    private TypeLoanOpenApi typeLoanOpenApi;
    private OrderOpenApi orderOpenApi;
    private StatusOpenApi statusOpenApi;
    private WebTestClient typeLoanWebTestClient;
    private WebTestClient statusWebTestClient;
    private WebTestClient orderWebTestClient;

    @BeforeEach
    void setUp() {
        typeLoanOpenApi = new TypeLoanOpenApi();
        orderOpenApi = new OrderOpenApi();
        statusOpenApi = new StatusOpenApi();

        RouterFunction<ServerResponse> typeLoanRouterFunction = typeLoanOpenApi.typeLoanRoutesDoc ();
        typeLoanWebTestClient = WebTestClient.bindToRouterFunction(typeLoanRouterFunction).build();

        RouterFunction<ServerResponse> orderRouterFunction = orderOpenApi.orderRoutesDoc ();
        orderWebTestClient = WebTestClient.bindToRouterFunction(orderRouterFunction).build();

        RouterFunction<ServerResponse> statusRouterFunction = statusOpenApi.statusRoutesDoc ();
        statusWebTestClient = WebTestClient.bindToRouterFunction(statusRouterFunction).build();
    }

    @Test
    void typeLoanRoutesDoc_shouldReturnOkForDummyRoute() {
        typeLoanWebTestClient.get()
                .uri("/__dummy__")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void typeLoanRoutesDoc_shouldNotBeNull() {
        RouterFunction<ServerResponse> routerFunction = typeLoanOpenApi.typeLoanRoutesDoc();
        assertThat(routerFunction).isNotNull();
    }

    @Test
    void statusLoanRoutesDoc_shouldReturnOkForDummyRoute() {
        statusWebTestClient.get()
                .uri("/__dummy__")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void statusRoutesDoc_shouldNotBeNull() {
        RouterFunction<ServerResponse> routerFunction = statusOpenApi.statusRoutesDoc ();
        assertThat(routerFunction).isNotNull();
    }

    @Test
    void orderLoanRoutesDoc_shouldReturnOkForDummyRoute() {
        orderWebTestClient.get()
                .uri("/__dummy__")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void orderRoutesDoc_shouldNotBeNull() {
        RouterFunction<ServerResponse> routerFunction = orderOpenApi.orderRoutesDoc();
        assertThat(routerFunction).isNotNull();
    }
}
