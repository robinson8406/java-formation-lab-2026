package com.indra.retail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    @DisplayName("Debe rechazar un precio nulo")
    void shouldRejectNullPrice() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> Money.of(null));

        assertEquals("El precio no puede ser null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1.00", "-0.01"})
    @DisplayName("Debe rechazar un precio negativo")
    void shouldRejectNegativePrice(String invalidAmount) {
        var amount = new BigDecimal(invalidAmount);

        var exception = assertThrows(IllegalArgumentException.class,
                () -> Money.of(amount));

        assertEquals("El precio no puede ser negativo", exception.getMessage());
    }

}
