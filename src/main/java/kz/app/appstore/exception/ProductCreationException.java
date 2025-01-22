package kz.app.appstore.exception;

public class ProductCreationException extends Exception {
    public ProductCreationException(String message) {
        super(message);
    }
    public ProductCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
