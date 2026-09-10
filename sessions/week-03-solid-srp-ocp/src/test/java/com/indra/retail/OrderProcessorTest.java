package com.indra.retail;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderProcessorTest {

    private StockValidator stockValidator;
    private OrderNotifier orderNotifier;
    private OrderProcessor orderProcessor;

    @BeforeEach
    void setUp() {
        stockValidator = new StockValidator();
        orderNotifier = new OrderNotifier();
        orderProcessor = new OrderProcessor(stockValidator, orderNotifier);
    }

    @Test 
    public void orderProcessorShouldApplyDiscountSeasonal() {
        BigDecimal price = BigDecimal.valueOf(50_000);
        DiscountType discountType = DiscountType.SEASONAL;
        int requestedQuantity = 2;

        Order order = new Order("123", price, discountType, requestedQuantity, "testUser1@gmail.com");

        BigDecimal finalPrice = orderProcessor.process(order, requestedQuantity);

        BigDecimal expectedFinalPrice = BigDecimal.valueOf(90_000);
        assertEquals(0, finalPrice.compareTo(expectedFinalPrice));
    }

    @Test 
    public void orderProcessorShouldApplyDiscountLoyalty() {
        BigDecimal price = BigDecimal.valueOf(150_000);
        DiscountType discountType = DiscountType.LOYALTY;
        int requestedQuantity = 1;

        Order order = new Order("456", price, discountType, requestedQuantity, "testUser2@gmail.com");

        BigDecimal finalPrice = orderProcessor.process(order, requestedQuantity);
        BigDecimal expectedFinalPrice = BigDecimal.valueOf(127500);
        assertEquals(0, finalPrice.compareTo(expectedFinalPrice));
    }

    @Test 
    public void orderProcessorShouldApplyDiscountStandard() {
        BigDecimal price = BigDecimal.valueOf(130_000);
        DiscountType discountType = DiscountType.STANDARD;
        int requestedQuantity = 1;

        Order order = new Order("789", price, discountType, requestedQuantity, "testUser3@gmail.com");

        BigDecimal finalPrice = orderProcessor.process(order, requestedQuantity);
        BigDecimal expectedFinalPrice = BigDecimal.valueOf(104_000);
        assertEquals(0, finalPrice.compareTo(expectedFinalPrice));
    }

    @Test 
    public void discountCalculatorTest(){

        BigDecimal price = BigDecimal.valueOf(100_000);
        DiscountType standardDiscountType = DiscountType.STANDARD;
        DiscountCalculator discountCalculator = new DiscountCalculator();
        BigDecimal finalPrice = discountCalculator.calculateDiscount(price, standardDiscountType);
        BigDecimal expectedFinalPrice = BigDecimal.valueOf(80_000);
        assertEquals(0, finalPrice.compareTo(expectedFinalPrice));
    }

    @Test
    public void orderShouldRejectNullPrice() {
        assertThrows(NullPointerException.class,
                () -> new Order("101", null, DiscountType.LOYALTY, 1, "testUser4@gmail.com"));
    }

}
