package com.indra.catalog.suppliers.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class SupplierTest {
    @Test
    void normalizesNameToUppercaseAndTaxIdWithoutDots() {
        Supplier supplier = new Supplier("SUP-001", "  acme ltda ", " 900.123.456-7 ", " compras@acme.co ", null);

        assertThat(supplier.name()).isEqualTo("ACME LTDA");
        assertThat(supplier.taxId()).isEqualTo("900123456-7");
        assertThat(supplier.email()).isEqualTo("compras@acme.co");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectsBlankName(String name) {
        assertThatThrownBy(() -> new Supplier("SUP-001", name, "900123456-7", "compras@acme.co", null))
                .isInstanceOf(InvalidSupplierException.class)
                .hasMessageContaining("razón social");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "900123456", "900123456-77", "ABC123456-7"})
    void rejectsTaxIdWithInvalidFormat(String taxId) {
        assertThatThrownBy(() -> new Supplier("SUP-001", "ACME", taxId, "compras@acme.co", null))
                .isInstanceOf(InvalidSupplierException.class)
                .hasMessageContaining("NIT");
    }

    @Test
    void rejectsMissingIdOrEmail() {
        assertThatThrownBy(() -> new Supplier(null, "ACME", "900123456-7", "compras@acme.co", null))
                .isInstanceOf(InvalidSupplierException.class);
        assertThatThrownBy(() -> new Supplier("SUP-001", "ACME", "900123456-7", " ", null))
                .isInstanceOf(InvalidSupplierException.class);
    }
}
