package com.indra.retail.orders.web;

import com.indra.retail.orders.dto.ErrorResponse;
import com.indra.retail.orders.service.OrderNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleOrderNotFound debe retornar 404 y mensaje descriptivo en el contrato ErrorResponse")
    void handleOrderNotFound_returns404AndDescriptiveMessage() {
        OrderNotFoundException ex = new OrderNotFoundException("order-999");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleOrderNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
        assertTrue(response.getBody().errors().contains("Pedido no encontrado: order-999"));
    }

    @Test
    @DisplayName("handleMethodArgumentNotValid debe retornar 400 y formato campo: mensaje")
    void handleMethodArgumentNotValid_returns400AndFormattedFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("createOrderRequest", "deliveryAddress", "mínimo 10 caracteres");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertTrue(response.getBody().errors().contains("deliveryAddress: mínimo 10 caracteres"));
    }

    @Test
    @DisplayName("handleGenericException debe retornar 500 y mensaje genérico sin exponer detalles internos")
    void handleGenericException_returns500AndGenericMessage() {
        Exception ex = new NullPointerException("Null reference inside internal pipeline");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals(List.of("Error interno del servidor"), response.getBody().errors());
    }
}
