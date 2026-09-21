package com.zamtrust.exception;

/**
 * Thrown when a document is expected to be a PDF but isn't.
 * Mapped to HTTP 400 (client error, not server error).
 */
public class NotAPdfException extends RuntimeException {
    public NotAPdfException(String message) {
        super(message);
    }
}
