package com.zamtrust.exception;

import com.zamtrust.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String UPLOAD_TOO_LARGE_MSG =
            "File exceeds the 25 MB upload limit. Please reduce the file size and try again.";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiError> invalidFile(InvalidFileException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<ApiError> fileTooLarge(FileTooLargeException ex, HttpServletRequest req) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, ex.getMessage(), req);
    }

    @ExceptionHandler(CryptoException.class)
    public ResponseEntity<ApiError> crypto(CryptoException ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> illegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /**
     * Catch-all for the many ways Spring / Tomcat surface oversized uploads.
     * The exception class hierarchy varies between versions:
     *   - MaxUploadSizeExceededException
     *   - MultipartException (wraps Tomcat's SizeLimitExceededException)
     *   - FileSizeLimitExceededException (Tomcat's internal class)
     *   - SizeLimitExceededException (Apache Commons FileUpload)
     *
     * We detect by class name and message so a single handler covers them all.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
        if (isUploadSizeExceeded(ex)) {
            return build(HttpStatus.PAYLOAD_TOO_LARGE, UPLOAD_TOO_LARGE_MSG, req);
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() != null ? ex.getMessage() : "Unexpected error", req);
    }

    private boolean isUploadSizeExceeded(Throwable ex) {
        Throwable cause = ex;
        int depth = 0;
        while (cause != null && depth < 20) {
            String name = cause.getClass().getName();
            if (name.contains("MaxUploadSizeExceeded")
                    || name.contains("SizeLimitExceeded")
                    || name.contains("FileSizeLimitExceeded")) {
                return true;
            }
            String msg = cause.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("maximum upload size")
                        || lower.contains("size limit")
                        || lower.contains("exceeds")) {
                    return true;
                }
            }
            cause = cause.getCause();
            depth++;
        }
        return false;
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String msg, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(), msg, req.getRequestURI()));
    }
}
