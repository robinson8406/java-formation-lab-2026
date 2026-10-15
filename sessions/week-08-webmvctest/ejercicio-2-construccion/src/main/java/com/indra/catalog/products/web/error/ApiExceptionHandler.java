package com.indra.catalog.products.web.error;

import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(ProductNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", List.of(exception.getMessage()), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", errors, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", List.of("Error interno del servidor"), request);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String code, List<String> errors, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(Instant.now(), status.value(), code, errors, request.getRequestURI()));
    }
}
