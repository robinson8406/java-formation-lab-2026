package com.indra.catalog.products.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProductTest {

    @Test
    void trimsName() {
        Product product = new Product("PRD-0001", "  Laptop  ", BigDecimal.TEN, 0);

        assertThat(product.name()).isEqualTo("Laptop");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void rejectsNonPositivePrice(String price) {
        assertThatThrownBy(() -> new Product("PRD-0001", "Laptop", new BigDecimal(price), 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeStock() {
        assertThatThrownBy(() -> new Product("PRD-0001", "Laptop", BigDecimal.TEN, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> new Product("PRD-0001", " ", BigDecimal.TEN, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
