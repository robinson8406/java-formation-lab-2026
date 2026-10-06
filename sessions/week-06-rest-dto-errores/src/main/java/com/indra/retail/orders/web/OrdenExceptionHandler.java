package com.indra.retail.orders.web;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.indra.retail.orders.service.OrderNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class OrdenExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleExceptionArgumentNotValid(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleExceptionNotFound(OrderNotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, List.of(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> globalExceptionHandler(
            Exception exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, List.of("Ocurrio un error interno"));
    }

    private ResponseEntity<ErrorMessage> buildResponse(HttpStatus status, List<String> errors) {
        return ResponseEntity.status(status).body(new ErrorMessage(Instant.now(), status.value(), errors));
    }
}
