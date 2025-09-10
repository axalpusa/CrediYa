package co.com.pragma.api.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.NotFoundException;
import exceptions.UnauthorizedException;
import exceptions.ValidationException;
import io.netty.handler.timeout.TimeoutException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Component
@Slf4j
@AllArgsConstructor
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono < Void > handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        Map<String, Object> response;

        if ( ex instanceof ValidationException ve ) {
            status = HttpStatus.BAD_REQUEST;
            response = Map.of(
                    "message", "Validation Error",
                    "details", ve.getErrors()
            );
        } else if ( ex instanceof IllegalArgumentException ) {
            status = HttpStatus.BAD_REQUEST;
            response = Map.of ( "message", ex.getMessage ( ) );
        }else if (ex instanceof UnauthorizedException ) {
            status = HttpStatus.UNAUTHORIZED;
            response = Map.of("message", "Unauthorized", "details", ex.getMessage());
        } else if (ex instanceof NotFoundException ) {
            status = HttpStatus.NOT_FOUND;
            response = Map.of("message", "Not found", "details", ex.getMessage());
        }else if (ex instanceof DuplicateKeyException ) {
            status = HttpStatus.CONFLICT;
            response = Map.of("message", "Conflict", "details", ex.getMessage());
        }else if (ex instanceof AccessDeniedException ) {
            status = HttpStatus.FORBIDDEN;
            response = Map.of("message", "Forbidden", "details", ex.getMessage());
        }else if (ex instanceof JsonProcessingException ) {
            status = HttpStatus.BAD_REQUEST;
            response = Map.of("message", "Invalid JSON format", "details", ((JsonProcessingException) ex).getOriginalMessage());
        }else if (ex instanceof TimeoutException ) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            response = Map.of("message", "Request timeout", "details", ex.getMessage());
        }else if (ex instanceof ConstraintViolationException cve) {
            status = HttpStatus.BAD_REQUEST;
            response = Map.of(
                    "message", "Validation error",
                    "details", cve.getConstraintViolations()
                            .stream()
                            .map( ConstraintViolation::getMessage)
                            .toList()
            );
        }else if (ex instanceof UnsupportedMediaTypeStatusException ) {
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            response = Map.of("message", "Unsupported media type", "details", ex.getMessage());
        }else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            response = Map.of(
                    "message", "Unexpected error occurred",
                    "details", ex.getMessage(),
                    "timestamp", Instant.now().toString()
            );
        }

        exchange.getResponse ( ).setStatusCode ( status );
        exchange.getResponse ( ).getHeaders ( ).setContentType ( MediaType.APPLICATION_JSON );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(response);
        } catch (Exception e) {
            log.error("Error serializing response for exception {}", ex.getClass().getSimpleName(), e);
            bytes = ("{\"message\":\"Internal serialization error\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = exchange.getResponse ( ).bufferFactory ( ).wrap ( bytes );
        return exchange.getResponse ( ).writeWith ( Mono.just ( buffer ) );

    }
    private String safeMessage(Throwable ex) {
        return ex.getMessage() != null ? ex.getMessage() : "No details available";
    }
}
