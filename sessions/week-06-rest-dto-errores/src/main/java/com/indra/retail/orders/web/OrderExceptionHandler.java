package com.indra.retail.orders.web;

import com.indra.retail.orders.service.OrderNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce todas las excepciones a un {@link ErrorResponse} con el mismo formato.
 * Ninguna respuesta incluye stack trace ni detalles internos; los errores inesperados
 * se registran en el log del servidor para poder investigarlos.
 */
@RestControllerAdvice
public class OrderExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderExceptionHandler.class);

    private final MessageSource messageSource;

    public OrderExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /** 400: el body no cumple las validaciones de Bean Validation. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", errors, request);
    }

    /** 400: un parámetro de path o query no cumple sus restricciones (@Validated en el controller). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<String> errors = exception.getConstraintViolations().stream()
                .map(violation -> parameterName(violation) + ": " + violation.getMessage())
                .sorted()
                .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", errors, request);
    }

    /** 400: el body no es un JSON válido. Es un error del cliente, no del servidor. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                List.of(message("error.malformed-request")), request);
    }

    /** 404: el pedido no existe. */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            OrderNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND",
                List.of(message("error.order-not-found", exception.getOrderId())), request);
    }

    /**
     * Cualquier otra excepción. Las excepciones estándar de Spring MVC (405, 415, 404 de
     * ruta inexistente...) conservan su código HTTP; el resto es un 500 genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception, HttpServletRequest request) {
        if (exception instanceof org.springframework.web.ErrorResponse springError) {
            HttpStatus status = HttpStatus.resolve(springError.getStatusCode().value());
            if (status != null && status != HttpStatus.INTERNAL_SERVER_ERROR) {
                return response(status, status.name(), List.of(status.getReasonPhrase()), request);
            }
        }
        log.error("Error inesperado en {} {}", request.getMethod(), request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                List.of(message("error.internal")), request);
    }

    private String parameterName(ConstraintViolation<?> violation) {
        String name = "";
        for (Path.Node node : violation.getPropertyPath()) {
            name = node.getName();
        }
        return name;
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status, String code, List<String> errors, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(Instant.now(), status.value(), code, errors, request.getRequestURI()));
    }
}
