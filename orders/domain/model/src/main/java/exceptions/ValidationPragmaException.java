package exceptions;

import java.util.List;

public class ValidationPragmaException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final List < String > errors;

    public ValidationPragmaException(List < String > errors) {
        super ( String.join ( ", ", errors ) );
        this.errors = errors;
    }

    public ValidationPragmaException(String message, List < String > errors) {
        super ( message );
        this.errors = errors;
    }

    public ValidationPragmaException(List < String > errors, Throwable cause) {
        super ( String.join ( ", ", errors ), cause );
        this.errors = errors;
    }

    public List < String > getErrors() {
        return List.copyOf ( errors );
    }
}
