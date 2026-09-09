package com.indra.retail;

import com.indra.retail.config.DiscountCalculatorFactory;
import com.indra.retail.discount.DiscountCalculator;
import com.indra.retail.domain.DiscountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("DiscountCalculator")
class DiscountCalculatorTest {

    private final DiscountCalculator calculator = DiscountCalculatorFactory.createDefault();

    @Test
    @DisplayName("Aplica el descuento STANDARD del 5%")
    void appliesStandardDiscount() {
        BigDecimal result = calculator.apply(BigDecimal.valueOf(100), DiscountType.STANDARD);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(95.00)));
    }

    @Test
    @DisplayName("Aplica el descuento SEASONAL del 20%")
    void appliesSeasonalDiscount() {
        BigDecimal result = calculator.apply(BigDecimal.valueOf(100), DiscountType.SEASONAL);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(80.00)));
    }

    @Test
    @DisplayName("Aplica el descuento LOYALTY del 15% cuando la antigüedad supera los 12 meses")
    void appliesLoyaltyDiscountWhenMoreThan12Months() {
        BigDecimal result = calculator.apply(BigDecimal.valueOf(100), DiscountType.LOYALTY, 13);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(85.00)));
    }

    @Test
    @DisplayName("No aplica el descuento LOYALTY cuando la antigüedad es de 12 meses o menos")
    void doesNotApplyLoyaltyDiscountWhen12MonthsOrLess() {
        BigDecimal result = calculator.apply(BigDecimal.valueOf(100), DiscountType.LOYALTY, 12);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(100)));
    }
}