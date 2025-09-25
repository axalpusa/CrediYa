package co.com.pragma.api.config;

import exceptions.BadRequestPragmaException;
import exceptions.NotFoundPragmaException;
import exceptions.UnauthorizedPragmaException;
import exceptions.ValidationPragmaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationPragmaException.class)
    public ResponseEntity < Map < String, Object > > handleValidationException(ValidationPragmaException ex) {
        Map < String, Object > response = new HashMap <> ( );
        response.put ( "status", HttpStatus.BAD_REQUEST.value ( ) );
        response.put ( "message", "Validation error" );
        response.put ( "errors", ex.getErrors ( ) );
        return ResponseEntity.status ( HttpStatus.BAD_REQUEST ).body ( response );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity < Map < String, Object > > handleIllegalArgumentException(IllegalArgumentException ex) {
        Map < String, Object > response = new HashMap <> ( );
        response.put ( "status", HttpStatus.BAD_REQUEST.value ( ) );
        response.put ( "message", ex.getMessage ( ) );
        return ResponseEntity.status ( HttpStatus.BAD_REQUEST ).body ( response );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity < Map < String, Object > > handleGenericException(Exception ex) {
        Map < String, Object > response = new HashMap <> ( );
        response.put ( "status", HttpStatus.INTERNAL_SERVER_ERROR.value ( ) );
        response.put ( "message", "Unexpected error occurred" );
        response.put ( "details", ex.getMessage ( ) );
        return ResponseEntity.status ( HttpStatus.INTERNAL_SERVER_ERROR ).body ( response );
    }
    @ExceptionHandler(UnauthorizedPragmaException.class)
    public Mono <Map<String, Object>> handleUnauthorized(UnauthorizedPragmaException ex) {
        return Mono.just(Map.of(
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(NotFoundPragmaException.class)
    public Mono<Map<String, Object>> handleNotFound(NotFoundPragmaException ex) {
        return Mono.just(Map.of(
                "status", HttpStatus.NOT_FOUND.value(),
                "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(BadRequestPragmaException.class)
    public Mono<Map<String, Object>> handleBadRequest(BadRequestPragmaException ex) {
        return Mono.just(Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<Map<String, Object>> handleGeneric(RuntimeException ex) {
        return Mono.just(Map.of(
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Unexpected error: " + ex.getMessage()
        ));
    }
}
