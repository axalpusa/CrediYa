package co.com.pragma.api;

import co.com.pragma.api.services.AuthServiceClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.UUID;

class AuthServiceClientTest {

    private MockWebServer mockWebServer;
    private AuthServiceClient authServiceClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer ( );
        mockWebServer.start ( );

        String baseUrl = mockWebServer.url ( "/" ).toString ( );
        authServiceClient = new AuthServiceClient ( WebClient.builder ( ), baseUrl );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown ( );
    }

    @Test
    void validateToken_shouldReturnAuthResponseDTO() {
        String idUser = UUID.randomUUID().toString();
        String idRol = UUID.randomUUID().toString();

        String json = """
            {
              "idUser": "%s",
              "idRol": "%s",
              "name": "testuser",
              "token": "fake-token"
            }
            """.formatted(idUser, idRol);

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(authServiceClient.validateToken("fake-token"))
                .expectNextMatches(resp ->
                        resp.getName().equals("testuser") &&
                                resp.getToken().equals("fake-token") &&
                                resp.getIdUser().toString().equals(idUser) &&
                                resp.getIdRol().toString().equals(idRol))
                .verifyComplete();
    }


}