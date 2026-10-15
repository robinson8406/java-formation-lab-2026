package com.indra.catalog.products.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.indra.catalog.products.domain.exception.InvalidProductException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ProductTest {
    private static final BigDecimal PRICE = new BigDecimal("3500000.00");

    @Test
    void create_withValidData_keepsValuesAndTrimsName() {
        Product product = new Product("PRD-0001", "  Laptop  ", PRICE, 10);

        assertThat(product.id()).isEqualTo("PRD-0001");
        assertThat(product.name()).isEqualTo("Laptop");
        assertThat(product.price()).isEqualByComparingTo("3500000");
        assertThat(product.stock()).isEqualTo(10);
    }

    @Test
    void create_withZeroStock_isAllowed() {
        assertThat(new Product("PRD-0001", "Laptop", PRICE, 0).stock()).isZero();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void create_withBlankName_isRejected(String name) {
        assertThatThrownBy(() -> new Product("PRD-0001", name, PRICE, 10))
                .isInstanceOf(InvalidProductException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    void create_withNameOf120Chars_isAllowed() {
        String name = "a".repeat(Product.MAX_NAME_LENGTH);

        assertThat(new Product("PRD-0001", name, PRICE, 10).name()).hasSize(Product.MAX_NAME_LENGTH);
    }

    @Test
    void create_withNameLongerThan120Chars_isRejected() {
        String name = "a".repeat(Product.MAX_NAME_LENGTH + 1);

        assertThatThrownBy(() -> new Product("PRD-0001", name, PRICE, 10))
                .isInstanceOf(InvalidProductException.class)
                .hasMessageContaining("120");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-1"})
    void create_withNonPositivePrice_isRejected(String price) {
        assertThatThrownBy(() -> new Product("PRD-0001", "Laptop", new BigDecimal(price), 10))
                .isInstanceOf(InvalidProductException.class)
                .hasMessageContaining("precio");
    }

    @Test
    void create_withNullPrice_isRejected() {
        assertThatThrownBy(() -> new Product("PRD-0001", "Laptop", null, 10))
                .isInstanceOf(InvalidProductException.class);
    }

    @Test
    void create_withNegativeStock_isRejected() {
        assertThatThrownBy(() -> new Product("PRD-0001", "Laptop", PRICE, -1))
                .isInstanceOf(InvalidProductException.class)
                .hasMessageContaining("stock");
    }
}
