package com.indra.retail;

import com.indra.retail.config.DiscountCalculatorFactory;
import com.indra.retail.domain.DiscountType;
import com.indra.retail.domain.Order;
import com.indra.retail.notification.OrderNotifier;
import com.indra.retail.processing.OrderProcessor;
import com.indra.retail.stock.StockValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("OrderProcessor")
class OrderProcessorTest {

    private OrderProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OrderProcessor(
                new StockValidator(),
                new OrderNotifier(),
                DiscountCalculatorFactory.createDefault());
    }

    @Test
    @DisplayName("Procesa el pedido aplicando el descuento LOYALTY correctamente")
    void processesOrderWithLoyaltyDiscount() {
        Order order = new Order("O-1", BigDecimal.valueOf(100), DiscountType.LOYALTY, 2, "cliente@test.com", 13);
        BigDecimal result = processor.process(order, 10);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(85.00)));
    }

    @Test
    @DisplayName("Lanza excepción cuando no hay stock suficiente")
    void failsWhenNotEnoughStock() {
        Order order = new Order("O-2", BigDecimal.valueOf(100), DiscountType.STANDARD, 5, "cliente@test.com");
        assertThrows(IllegalStateException.class, () -> processor.process(order, 1));
    }
}