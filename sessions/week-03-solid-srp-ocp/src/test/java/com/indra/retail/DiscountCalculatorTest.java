package com.indra.retail;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DiscountCalculatorTest {
    private final DiscountStrategyFactory factory = new DiscountStrategyFactory();
    private final DiscountCalculator discountCalculator = new DiscountCalculator(factory);

    @Test
    @DisplayName("Debe aplicar el descuento STANDARD al precio de la orden")
    void shouldApplyStandardDiscount() {
        Order order = new Order("ORDER1", BigDecimal.valueOf(1000), DiscountType.STANDARD,
                1, "pepe@gmail.com");
        BigDecimal priceDiscount = discountCalculator.apply(order.getPrice(), order.getDiscountType());
        assertTrue(priceDiscount.compareTo(BigDecimal.valueOf(950)) == 0);
    }

    @Test
    @DisplayName("Debe aplicar el descuento SEASONAL al precio de la orden")
    void shouldApplySeasonalDiscount() {
        Order order = new Order("ORDER1", BigDecimal.valueOf(1000), DiscountType.SEASONAL,
                1, "pepe@gmail.com");
        BigDecimal priceDiscount = discountCalculator.apply(order.getPrice(), order.getDiscountType());
        assertTrue(priceDiscount.compareTo(BigDecimal.valueOf(800)) == 0);
    }

    @Test
    @DisplayName("Debe aplicar el descuento LOYALTY al precio de la orden")
    void shouldApplyLoyaltyDiscount() {
        Order order = new Order("ORDER1", BigDecimal.valueOf(1000), DiscountType.LOYALTY,
                1, "pepe@gmail.com");
        BigDecimal priceDiscount = discountCalculator.apply(order.getPrice(), order.getDiscountType());
        assertTrue(priceDiscount.compareTo(BigDecimal.valueOf(850)) == 0);
    }
}
