package com.indra.retail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoyaltyTest {

    private final Loyalty discount = new Loyalty();

    @ParameterizedTest
    @CsvSource({
            "100.00,85.00",
            "49.99,42.49",
            "0.00,0.00"
    })
    @DisplayName("Debe aplicar el descuento loyalty del 15 por ciento")
    void shouldApplyLoyaltyDiscount(String originalPrice, String discountedPrice) {
        var price = Money.of(new BigDecimal(originalPrice));

        var result = discount.apply(price);

        assertEquals(Money.of(new BigDecimal(discountedPrice)), result);
    }

}
