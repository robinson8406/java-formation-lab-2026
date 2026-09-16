package com.indra.retail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiscountStrategiesTest {

    @Test
    @DisplayName("La estrategia estándar debe identificar su tipo")
    void standardStrategyShouldIdentifyItsType() {
        assertEquals(DiscountType.STANDARD, new StandardDiscountStrategy().supportedType());
    }

    @Test
    @DisplayName("La estrategia seasonal debe identificar su tipo")
    void seasonalStrategyShouldIdentifyItsType() {
        assertEquals(DiscountType.SEASONAL, new SeasonalDiscountStrategy().supportedType());
    }

    @Test
    @DisplayName("La estrategia loyalty debe identificar su tipo")
    void loyaltyStrategyShouldIdentifyItsType() {
        assertEquals(DiscountType.LOYALTY, new LoyaltyDiscountStrategy().supportedType());
    }

    @Test
    @DisplayName("La estrategia loyalty debe conservar el precio sin antigüedad suficiente")
    void loyaltyStrategyShouldKeepPriceWithoutEnoughMonths() {
        BigDecimal result = new LoyaltyDiscountStrategy()
                .apply(new BigDecimal("100.00"), 12);

        assertEquals(0, result.compareTo(new BigDecimal("100.00")));
    }
}
