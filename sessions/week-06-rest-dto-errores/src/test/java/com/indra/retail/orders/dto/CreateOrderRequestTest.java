package com.indra.retail.orders.dto;

import com.indra.retail.orders.model.OrderItem;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateOrderRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando todos los campos son válidos")
    void validate_validRequest_hasNoViolations() {
        List<OrderItem> items = List.of(new OrderItem("SKU-100", 1, 10.0));
        CreateOrderRequest request = new CreateOrderRequest("CUST-1", items, "Direccion Valida 1234");

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Debe fallar validación cuando customerId es nulo o vacío")
    void validate_nullOrEmptyCustomerId_hasViolations() {
        List<OrderItem> items = List.of(new OrderItem("SKU-100", 1, 10.0));
        CreateOrderRequest nullCustomer = new CreateOrderRequest(null, items, "Direccion Valida 1234");
        CreateOrderRequest emptyCustomer = new CreateOrderRequest("", items, "Direccion Valida 1234");

        assertFalse(validator.validate(nullCustomer).isEmpty());
        assertFalse(validator.validate(emptyCustomer).isEmpty());
    }

    @Test
    @DisplayName("Debe fallar validación cuando items es nulo o lista vacía")
    void validate_nullOrEmptyItems_hasViolations() {
        CreateOrderRequest nullItems = new CreateOrderRequest("CUST-1", null, "Direccion Valida 1234");
        CreateOrderRequest emptyItems = new CreateOrderRequest("CUST-1", Collections.emptyList(), "Direccion Valida 1234");

        assertFalse(validator.validate(nullItems).isEmpty());
        assertFalse(validator.validate(emptyItems).isEmpty());
    }

    @Test
    @DisplayName("Debe fallar validación cuando deliveryAddress es nulo o menor a 10 caracteres")
    void validate_invalidDeliveryAddress_hasViolations() {
        List<OrderItem> items = List.of(new OrderItem("SKU-100", 1, 10.0));
        CreateOrderRequest nullAddress = new CreateOrderRequest("CUST-1", items, null);
        CreateOrderRequest shortAddress = new CreateOrderRequest("CUST-1", items, "Corta 12");

        assertFalse(validator.validate(nullAddress).isEmpty());
        assertFalse(validator.validate(shortAddress).isEmpty());
    }
}
