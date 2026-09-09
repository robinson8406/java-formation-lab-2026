package com.indra.retail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardTest {

    private final Standard discount = new Standard();

    @ParameterizedTest
    @CsvSource({
            "100.00,95.00",
            "49.99,47.49",
            "0.00,0.00"
    })
    @DisplayName("Debe aplicar el descuento estandar del 5 por ciento")
    void shouldApplyStandardDiscount(String originalPrice, String discountedPrice) {
        var price = Money.of(new BigDecimal(originalPrice));

        var result = discount.apply(price);

        assertEquals(Money.of(new BigDecimal(discountedPrice)), result);
    }

}
