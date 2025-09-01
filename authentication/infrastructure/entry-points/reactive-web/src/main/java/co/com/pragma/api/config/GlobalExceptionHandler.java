package co.com.pragma.api.config;

import exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity < Map < String, Object > > handleValidationException(ValidationException ex) {
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
}
