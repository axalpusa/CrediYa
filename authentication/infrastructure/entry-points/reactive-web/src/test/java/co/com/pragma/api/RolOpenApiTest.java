package co.com.pragma.api;

import co.com.pragma.api.openapi.RolOpenApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RolOpenApiTest {

    private RolOpenApi rolOpenApi;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        rolOpenApi = new RolOpenApi();

        RouterFunction<ServerResponse> routerFunction = rolOpenApi.rolRoutesDoc();
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void rolRoutesDoc_shouldReturnOkForDummyRoute() {
        webTestClient.get()
                .uri("/__dummy__")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void rolRoutesDoc_shouldNotBeNull() {
        RouterFunction<ServerResponse> routerFunction = rolOpenApi.rolRoutesDoc();
        assertThat(routerFunction).isNotNull();
    }
}
