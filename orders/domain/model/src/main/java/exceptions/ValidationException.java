package exceptions;

import java.util.List;

public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final List < String > errors;

    public ValidationException(List < String > errors) {
        super ( String.join ( ", ", errors ) );
        this.errors = errors;
    }

    public ValidationException(String message, List < String > errors) {
        super ( message );
        this.errors = errors;
    }

    public ValidationException(List < String > errors, Throwable cause) {
        super ( String.join ( ", ", errors ), cause );
        this.errors = errors;
    }

    public List < String > getErrors() {
        return List.copyOf ( errors );
    }
}
