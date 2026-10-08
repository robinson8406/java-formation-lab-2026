package com.indra.retail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockValidatorTest {

    private final StockValidator stockValidator = new StockValidator();

    @Test
    @DisplayName("Debe permitir el pedido cuando hay stock suficiente")
    void shouldAllowWhenStockIsSufficient() {
        assertTrue(stockValidator.hasEnoughStock(10, 5));
    }

    @Test
    @DisplayName("Debe rechazar el pedido cuando el stock es insuficiente")
    void shouldRejectWhenStockIsInsufficient() {
        assertFalse(stockValidator.hasEnoughStock(2, 5));
    }

    @Test
    @DisplayName("Debe permitir el pedido cuando el stock es exactamente igual al solicitado")
    void shouldAllowWhenStockEqualsRequested() {
        assertTrue(stockValidator.hasEnoughStock(5, 5));
    }
}
