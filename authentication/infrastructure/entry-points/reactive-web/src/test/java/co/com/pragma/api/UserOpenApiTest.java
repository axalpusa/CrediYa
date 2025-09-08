package co.com.pragma.api;

import co.com.pragma.api.openapi.UserOpenApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.assertj.core.api.Assertions.assertThat;

class UserOpenApiTest {

    private UserOpenApi userOpenApi;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        userOpenApi = new UserOpenApi();

        // Usamos la ruta dummy para probar que se levanta correctamente
        RouterFunction<ServerResponse> routerFunction = userOpenApi.userRoutesDoc();
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void userRoutesDoc_shouldReturnOkForDummyRoute() {
        webTestClient.get()
                .uri("/__dummy__")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void userRoutesDoc_shouldNotBeNull() {
        RouterFunction<ServerResponse> routerFunction = userOpenApi.userRoutesDoc();
        assertThat(routerFunction).isNotNull();
    }
}
