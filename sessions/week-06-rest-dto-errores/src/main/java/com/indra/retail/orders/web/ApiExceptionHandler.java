package com.indra.retail.orders.web;

import com.indra.retail.orders.service.OrderNotFoundException;
import com.indra.retail.orders.web.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
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
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .sorted()
                .toList();
        return errorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        List<String> errors = exception.getConstraintViolations().stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    String field = path.substring(path.lastIndexOf('.') + 1);
                    return field + ": " + violation.getMessage();
                })
                .sorted()
                .toList();
        return errorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(
            HandlerMethodValidationException exception) {
        List<String> errors = exception.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> formatMethodError(result.getMethodParameter().getParameterName(), error)))
                .sorted()
                .toList();
        return errorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotFound(OrderNotFoundException exception) {
        String message = messageSource.getMessage(
                "order.notFound",
                new Object[]{exception.getOrderId()},
                LocaleContextHolder.getLocale());
        return errorResponse(HttpStatus.NOT_FOUND, List.of(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
        LOGGER.error("Unhandled error while processing the request", exception);
        String message = messageSource.getMessage(
                "error.internal",
                null,
                LocaleContextHolder.getLocale());
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, List.of(message));
    }

    private String formatFieldError(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();
        return fieldError.getField() + ": " + (message == null ? "" : message);
    }

    private String formatMethodError(String field, MessageSourceResolvable error) {
        String message = messageSource.getMessage(error, LocaleContextHolder.getLocale());
        return field + ": " + message;
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, List<String> errors) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(Instant.now(), status.value(), errors));
    }
}
