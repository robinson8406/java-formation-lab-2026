package com.indra.retail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeasonalTest {

    private final Seasonal discount = new Seasonal();

    @ParameterizedTest
    @CsvSource({
            "100.00,80.00",
            "49.99,39.99",
            "0.00,0.00"
    })
    @DisplayName("Debe aplicar el descuento seasonal del 20 por ciento")
    void shouldApplySeasonalDiscount(String originalPrice, String discountedPrice) {
        var price = Money.of(new BigDecimal(originalPrice));

        var result = discount.apply(price);

        assertEquals(Money.of(new BigDecimal(discountedPrice)), result);
    }

}
