package com.indra.retail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProcessorTest {

    private final StockValidator stockValidator = new StockValidator();
    private final OrderNotifier orderNotifier = new OrderNotifier();
    private final OrderProcessor orderProcessor = new OrderProcessor(stockValidator, orderNotifier);

    @Test
    @DisplayName("Debe procesar el pedido sin descuento cuando no hay tipo de descuento")
    void shouldProcessOrderWithoutDiscount() {
        var order = new Order("ORD-001", new BigDecimal("100.00"), null, 2, "cliente@indra.com");

        var result = orderProcessor.process(order, 2);

        assertEquals(new BigDecimal("100.00"), result);
    }

    @ParameterizedTest
    @CsvSource({
            "STANDARD,100.00,95.0000",
            "SEASONAL,100.00,80.000"
    })
    @DisplayName("Debe aplicar el descuento configurado segun el tipo")
    void shouldApplyDiscountByType(DiscountType discountType, String price, String expectedTotal) {
        var order = new Order("ORD-002", new BigDecimal(price), discountType, 1, "cliente@indra.com");

        var result = orderProcessor.process(order, 5);

        assertEquals(new BigDecimal(expectedTotal), result);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Debe fallar cuando no hay stock suficiente para el pedido")
    void shouldThrowWhenStockIsInsufficient(int availableStock) {
        var order = new Order("ORD-003", new BigDecimal("50.00"), DiscountType.STANDARD, 2, "cliente@indra.com");

        var exception = assertThrows(IllegalStateException.class,
                () -> orderProcessor.process(order, availableStock));

        assertEquals("Stock insuficiente para el pedido ORD-003", exception.getMessage());
    }
}
