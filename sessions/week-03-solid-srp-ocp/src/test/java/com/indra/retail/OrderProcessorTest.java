package com.indra.retail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderProcessorTest {

    private final DiscountCalculator discountCalculator = new DiscountCalculator(List.of(
            new StandardDiscountStrategy(),
            new SeasonalDiscountStrategy(),
            new LoyaltyDiscountStrategy()));
    private final OrderProcessor orderProcessor = new OrderProcessor(
            new StockValidator(), discountCalculator, new OrderNotifier());

    @Test
    @DisplayName("Debe procesar el pedido aplicando el descuento de la orden")
    void shouldProcessOrderWithItsDiscount() {
        Order order = new Order("ORD-1", new BigDecimal("100.00"),
                DiscountType.STANDARD, 2, "cliente@indra.com", 6);

        BigDecimal result = orderProcessor.process(order, 2);

        assertEquals(0, result.compareTo(new BigDecimal("95.00")));
    }

    @Test
    @DisplayName("Debe rechazar el pedido cuando no hay stock suficiente")
    void shouldRejectOrderWhenStockIsInsufficient() {
        Order order = new Order("ORD-2", new BigDecimal("100.00"),
                DiscountType.STANDARD, 3, "cliente@indra.com", 6);

        assertThrows(IllegalStateException.class, () -> orderProcessor.process(order, 2));
    }

    @Test
    @DisplayName("Debe procesar loyalty para un cliente con más de doce meses")
    void shouldProcessLoyaltyOrderForEligibleCustomer() {
        Order order = new Order("ORD-3", new BigDecimal("100.00"),
                DiscountType.LOYALTY, 1, "cliente@indra.com", 13);

        BigDecimal result = orderProcessor.process(order, 1);

        assertEquals(0, result.compareTo(new BigDecimal("85.00")));
    }
}
