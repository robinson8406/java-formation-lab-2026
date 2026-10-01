package com.indra.retail.orders.web;

import com.indra.retail.orders.service.OrderNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final MessageSource messageSource;

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                              Locale locale) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> formatFieldError(error, locale))
                .sorted()
                .toList();
        return errorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception,
                                                                       Locale locale) {
        List<String> errors = exception.getConstraintViolations().stream()
                .map(violation -> {
                    String field = violation.getPropertyPath().toString();
                    field = field.substring(field.lastIndexOf('.') + 1);
                    String messageKey = violation.getMessageTemplate().replace("{", "").replace("}", "");
                    String message = messageSource.getMessage(messageKey, null, violation.getMessage(), locale);
                    return field + ": " + message;
                })
                .sorted()
                .toList();
        return errorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(HandlerMethodValidationException exception,
                                                                     Locale locale) {
        List<String> errors = exception.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> result.getMethodParameter().getParameterName() + ": "
                                + messageSource.getMessage(error, locale)))
                .sorted()
                .toList();
        return errorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(OrderNotFoundException exception, Locale locale) {
        String message = messageSource.getMessage("error.order.notFound", new Object[]{exception.getMessage()}, locale);
        return errorResponse(HttpStatus.NOT_FOUND, List.of(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, Locale locale) {
        LOGGER.error("Unexpected error while processing request", exception);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                List.of(messageSource.getMessage("error.internal", null, locale)));
    }

    private String formatFieldError(FieldError error, Locale locale) {
        return error.getField() + ": " + messageSource.getMessage(error, locale);
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, List<String> errors) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(Instant.now(), status.value(), errors));
    }
}