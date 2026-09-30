package com.indra.retail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DiscountStrategyTest {

    private final DiscountStrategy.Factory factory = DiscountStrategy.Factory.instance();

    @Test
    @DisplayName("Debe crear estrategia STANDARD y aplicar 5% descuento")
    void shouldCreateStandardStrategy() {
        DiscountStrategy strategy = factory.create(DiscountType.STANDARD);
        assertNotNull(strategy);

        BigDecimal result = strategy.apply(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("95.00").setScale(2), result.setScale(2));
    }

    @Test
    @DisplayName("Debe crear estrategia SEASONAL y aplicar 20% descuento")
    void shouldCreateSeasonalStrategy() {
        DiscountStrategy strategy = factory.create(DiscountType.SEASONAL);
        assertNotNull(strategy);

        BigDecimal result = strategy.apply(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("80.00").setScale(2), result.setScale(2));
    }

    @Test
    @DisplayName("Debe crear estrategia LOYALTY y aplicar 15% descuento")
    void shouldCreateLoyaltyStrategy() {
        DiscountStrategy strategy = factory.create(DiscountType.LOYALTY);
        assertNotNull(strategy);

        BigDecimal result = strategy.apply(new BigDecimal("200.00"));
        assertEquals(new BigDecimal("170.00").setScale(2), result.setScale(2));
    }

    @Test
    @DisplayName("Debe permitir agregar nuevo tipo de descuento sin modificar Factory existente")
    void shouldAllowNewDiscountWithoutModifyingFactory() {
        DiscountStrategy.StandardDiscount custom = new DiscountStrategy.StandardDiscount();
        BigDecimal result = custom.apply(new BigDecimal("50.00"));
        assertEquals(new BigDecimal("47.50").setScale(2), result.setScale(2));
    }

    @Test
    @DisplayName("Debe sellar hierarquía de DiscountStrategy")
    void shouldSealDiscountStrategyHierarchy() {
        DiscountStrategy.StandardDiscount standard = new DiscountStrategy.StandardDiscount();
        DiscountStrategy.SeasonalDiscount seasonal = new DiscountStrategy.SeasonalDiscount();
        DiscountStrategy.LoyaltyDiscount loyalty = new DiscountStrategy.LoyaltyDiscount();

        assertEquals(new BigDecimal("95.00").setScale(2), standard.apply(new BigDecimal("100.00")).setScale(2));
        assertEquals(new BigDecimal("80.00").setScale(2), seasonal.apply(new BigDecimal("100.00")).setScale(2));
        assertEquals(new BigDecimal("85.00").setScale(2), loyalty.apply(new BigDecimal("100.00")).setScale(2));
    }
}
