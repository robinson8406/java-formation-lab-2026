package com.indra.retail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiscountCalculatorTest {

    private final DiscountCalculator calculator = new DiscountCalculator(List.of(
            new StandardDiscountStrategy(),
            new SeasonalDiscountStrategy(),
            new LoyaltyDiscountStrategy()));

    @Test
    @DisplayName("Debe aplicar el descuento estándar")
    void shouldApplyStandardDiscount() {
        assertAmount("95.00", calculator.apply(new BigDecimal("100.00"), DiscountType.STANDARD));
    }

    @Test
    @DisplayName("Debe aplicar el descuento seasonal")
    void shouldApplySeasonalDiscount() {
        assertAmount("80.00", calculator.apply(new BigDecimal("100.00"), DiscountType.SEASONAL));
    }

    @Test
    @DisplayName("Debe aplicar loyalty cuando el cliente supera doce meses")
    void shouldApplyLoyaltyDiscountForCustomerWithMoreThanTwelveMonths() {
        assertAmount("85.00",
                calculator.apply(new BigDecimal("100.00"), DiscountType.LOYALTY, 13));
    }

    @Test
    @DisplayName("No debe aplicar loyalty cuando el cliente no supera doce meses")
    void shouldNotApplyLoyaltyDiscountForCustomerWithTwelveMonthsOrLess() {
        assertAmount("100.00",
                calculator.apply(new BigDecimal("100.00"), DiscountType.LOYALTY, 12));
    }

    private void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(new BigDecimal(expected)));
    }
}
