package com.indra.retail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscountCalculatorTest {

    private final DiscountCalculator discountCalculator = new DiscountCalculator();

    @Test
    @DisplayName("Debe aplicar descuento STANDARD del 5%")
    void shouldApplyStandardDiscount() {
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal result = discountCalculator.apply(price, DiscountType.STANDARD);
        assertEquals(new BigDecimal("95.00").setScale(2), result.setScale(2));
    }

    @Test
    @DisplayName("Debe aplicar descuento SEASONAL del 20%")
    void shouldApplySeasonalDiscount() {
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal result = discountCalculator.apply(price, DiscountType.SEASONAL);
        assertEquals(new BigDecimal("80.00").setScale(2), result.setScale(2));
    }

    @Test
    @DisplayName("Debe aplicar descuento LOYALTY del 15%")
    void shouldApplyLoyaltyDiscount() {
        BigDecimal price = new BigDecimal("200.00");
        BigDecimal result = discountCalculator.apply(price, DiscountType.LOYALTY);
        assertEquals(new BigDecimal("170.00").setScale(2), result.setScale(2));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el precio es nulo")
    void shouldThrowWhenPriceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> discountCalculator.apply(null, DiscountType.STANDARD));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el precio es cero")
    void shouldThrowWhenPriceIsZero() {
        assertThrows(IllegalArgumentException.class, () -> discountCalculator.apply(BigDecimal.ZERO, DiscountType.STANDARD));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el precio es negativo")
    void shouldThrowWhenPriceIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> discountCalculator.apply(new BigDecimal("-50"), DiscountType.STANDARD));
    }
}
