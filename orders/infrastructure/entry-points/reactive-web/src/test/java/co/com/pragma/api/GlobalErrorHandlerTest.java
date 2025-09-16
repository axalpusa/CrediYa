package co.com.pragma.api;

import co.com.pragma.api.config.GlobalErrorHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.ValidationPragmaException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import exceptions.NotFoundPragmaException;
import exceptions.UnauthorizedPragmaException;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GlobalErrorHandlerTest {

    private ObjectMapper objectMapper;
    private GlobalErrorHandler errorHandler;
    private ServerWebExchange exchange;
    private ServerHttpResponse response;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper ( );
        errorHandler = new GlobalErrorHandler ( objectMapper );

        exchange = mock ( ServerWebExchange.class );
        response = mock ( ServerHttpResponse.class );

        headers = new HttpHeaders ( );

        when ( exchange.getResponse ( ) ).thenReturn ( response );
        when ( response.getHeaders ( ) ).thenReturn ( headers );
        when ( response.bufferFactory ( ) ).thenReturn ( new DefaultDataBufferFactory ( ) );
        when ( response.writeWith ( any ( ) ) ).thenReturn ( Mono.empty ( ) );
    }

    @Test
    void testConstraintViolationException() throws Exception {
        ConstraintViolation < ? > violation = mock ( ConstraintViolation.class );
        when ( violation.getMessage ( ) ).thenReturn ( "Name must not be empty" );
        ConstraintViolationException ex = new ConstraintViolationException ( Set.of ( violation ) );

        testExceptionHandling ( ex, HttpStatus.BAD_REQUEST, "Validation error", "Name must not be empty" );
    }

    @Test
    void testValidationException() throws Exception {
        ValidationPragmaException ex = new ValidationPragmaException ( List.of ( "field1 must not be null" ) );
        testExceptionHandling ( ex, HttpStatus.BAD_REQUEST, "Validation Error", "field1" );
    }

    @Test
    void testIllegalArgumentException() throws Exception {
        IllegalArgumentException ex = new IllegalArgumentException ( "Invalid argument" );
        testExceptionHandling ( ex, HttpStatus.BAD_REQUEST, "Invalid argument" );
    }

    @Test
    void testUnknownException() throws Exception {
        RuntimeException ex = new RuntimeException ( "Something went wrong" );
        testExceptionHandling ( ex, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", "Something went wrong" );
    }

    private void testExceptionHandling(Throwable ex, HttpStatus expectedStatus, String... expectedContents) throws Exception {
        Mono < Void > result = errorHandler.handle ( exchange, ex );

        StepVerifier.create ( result ).verifyComplete ( );

        verify ( response ).setStatusCode ( expectedStatus );

        assertEquals ( MediaType.APPLICATION_JSON, headers.getContentType ( ) );

        ArgumentCaptor < byte[] > captor = ArgumentCaptor.forClass ( byte[].class );
        verify ( response ).writeWith ( any ( ) );
    }

    private String handleException(Throwable ex) {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        MockServerHttpResponse response = exchange.getResponse();

        errorHandler.handle(exchange, ex).block();

        return response.getBodyAsString().block();
    }

    @Test
    void shouldHandleValidationPragmaException() {
        ValidationPragmaException ex = new ValidationPragmaException(List.of("field1 is required"));
        String body = handleException(ex);

        assertThat(body).contains("Validation Error");
        assertThat(body).contains("field1 is required");
    }

    @Test
    void shouldHandleUnauthorizedPragmaException() {
        UnauthorizedPragmaException ex = new UnauthorizedPragmaException("No token");
        String body = handleException(ex);

        assertThat(body).contains("Unauthorized");
        assertThat(body).contains("No token");
    }

    @Test
    void shouldHandleNotFoundPragmaException() {
        NotFoundPragmaException ex = new NotFoundPragmaException("Not found resource");
        String body = handleException(ex);

        assertThat(body).contains("Not found");
        assertThat(body).contains("Not found resource");
    }

    @Test
    void shouldHandleUnsupportedMediaTypeException() {
        UnsupportedMediaTypeStatusException ex = new UnsupportedMediaTypeStatusException("bad media");
        String body = handleException(ex);

        assertThat(body).contains("Unsupported media type");
        assertThat(body).contains("bad media");
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Illegal arg");
        String body = handleException(ex);

        assertThat(body).contains("Illegal arg");
    }

    @Test
    void shouldHandleUnexpectedException() {
        RuntimeException ex = new RuntimeException("boom!");
        String body = handleException(ex);

        assertThat(body).contains("Unexpected error occurred");
        assertThat(body).contains("boom!");
    }
}
