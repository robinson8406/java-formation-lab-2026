package com.indra.retail;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiscountCalculatorTest {

    private final DiscountCalculator discountCalculator = new DiscountCalculator();

    @Test
    @DisplayName("Debe calcular descuento correctamente")
    void calcularDescuentoStandard() {
        Order order = new Order("uno", BigDecimal.valueOf(100), DiscountType.STANDARD, 1, "email@example.com");
        assertEquals(discountCalculator.calculateDiscount(order), new BigDecimal("95.00"));
    }

    @Test
    @DisplayName("Debe calcular descuento correctamente para SEASONAL")
    void calcularDescuentoSeasonal() {
        Order order = new Order("dos", BigDecimal.valueOf(100), DiscountType.SEASONAL, 1, "email@example.com");
        assertEquals(discountCalculator.calculateDiscount(order), new BigDecimal("80.0"));
    }
    @Test
    @DisplayName("Debe calcular descuento correctamente para LOYALTY")
    void calcularDescuentoLoyalty() {
        Order order = new Order("tres", BigDecimal.valueOf(100), DiscountType.LOYALTY, 1, "email@example.com");
        assertEquals(discountCalculator.calculateDiscount(order), new BigDecimal("85.00"));
    }
    
    @Test
    @DisplayName("Debe calcular descuento correctamente para DEFAULT")
    void calcularDescuentoDefault() {
        Order order = new Order("cuatro", BigDecimal.valueOf(100), DiscountType.DEFAULT, 1, "email@example.com");
        assertEquals(discountCalculator.calculateDiscount(order), new BigDecimal("100"));
    }
}
