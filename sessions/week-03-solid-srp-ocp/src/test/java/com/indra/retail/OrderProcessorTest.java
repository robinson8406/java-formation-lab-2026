package com.indra.retail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProcessorTest {

    private final StockValidator stockValidator = new StockValidator();
    private final OrderNotifier orderNotifier = new OrderNotifier();
    private final OrderProcessor orderProcessor = new OrderProcessor(stockValidator, orderNotifier);

    private static Stream<Arguments> discountScenarios() {
        return Stream.of(
                Arguments.of(null, "100.00", "100.00"),
                Arguments.of(new Standard(), "100.00", "95.0000"),
                Arguments.of(new Seasonal(), "100.00", "80.000"),
                Arguments.of(new Loyalty(), "100.00", "85.0000")
        );
    }

    @ParameterizedTest
    @MethodSource("discountScenarios")
    @DisplayName("Debe aplicar el descuento configurado según el tipo")
    void shouldApplyDiscountByType(DiscountCalculator discountType, String price, String expectedTotal) {
        var order = new Order("ORD-002", new BigDecimal(price), discountType, 1, "cliente@indra.com");

        var result = orderProcessor.process(order, 5);

        assertEquals(new BigDecimal(expectedTotal), result);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Debe fallar cuando no hay stock suficiente para el pedido")
    void shouldThrowWhenStockIsInsufficient(int availableStock) {
        var order = new Order("ORD-003", new BigDecimal("50.00"), new Standard(), 2, "cliente@indra.com");

        var exception = assertThrows(IllegalStateException.class,
                () -> orderProcessor.process(order, availableStock));

        assertEquals("Stock insuficiente para el pedido ORD-003", exception.getMessage());
    }

}
