package exceptions;

public class BadRequestPragmaException extends RuntimeException {
    public BadRequestPragmaException(String message) {
        super ( message );
    }
}
