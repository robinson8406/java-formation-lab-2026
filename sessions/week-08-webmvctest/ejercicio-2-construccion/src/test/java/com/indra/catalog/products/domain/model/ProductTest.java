package com.indra.catalog.products.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void normalizesNameAndRejectsInvalidPriceOrStock() {
        Product product = new Product(null, "  Laptop  ", new BigDecimal("3500000.00"), 10);

        assertEquals("Laptop", product.name());
        assertThrows(IllegalArgumentException.class,
                () -> new Product(null, "Laptop", BigDecimal.ZERO, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new Product(null, "Laptop", BigDecimal.ONE, -1));
    }
}
