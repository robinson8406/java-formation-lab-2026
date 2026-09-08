package com.indra.retail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
public class OrderProcessorTest {

    private OrderProcessor orderProcessor;
    private StockValidator stockValidator;
    private OrderNotifier orderNotifier;

    @BeforeEach
    public void beforeTests() {
        stockValidator = new StockValidator();
        orderNotifier = new OrderNotifier();
        orderProcessor = new OrderProcessor(stockValidator, orderNotifier);
    }

    @Test
    public void testLoyaltyDiscount() {
        assertNotNull(this.orderProcessor);
        Order order = new Order("TR435", new BigDecimal(100L), DiscountType.LOYALTY, 2, "andrusoft@mail.com");
        BigDecimal expectedValue = new BigDecimal(85L);
        BigDecimal total = orderProcessor.process(order, 4);
        assertEquals(expectedValue.longValue(), total.longValue());
    }
}
