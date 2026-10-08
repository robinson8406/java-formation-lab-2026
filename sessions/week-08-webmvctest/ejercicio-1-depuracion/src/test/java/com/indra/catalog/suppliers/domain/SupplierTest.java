package com.indra.catalog.suppliers.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Unitario puro (sin Spring): la normalización de compras es una regla de dominio. */
class SupplierTest {

    @Test
    void register_normalizaRazonSocialAMayusculasYRecortaEspacios() {
        Supplier supplier = Supplier.register("  acme ltda ", "900123456-7", "compras@acme.co");

        assertThat(supplier.name()).isEqualTo("ACME LTDA");
    }

    @Test
    void register_quitaLosPuntosDelNit() {
        Supplier supplier = Supplier.register("ACME LTDA", "900.123.456-7", "compras@acme.co");

        assertThat(supplier.taxId()).isEqualTo("900123456-7");
    }

    @Test
    void register_sinRazonSocial_lanzaExcepcion() {
        assertThatThrownBy(() -> Supplier.register(" ", "900123456-7", "compras@acme.co"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withId_conservaLosDemasDatosYSoloCambiaElId() {
        Supplier registered = Supplier.register("ACME LTDA", "900123456-7", "compras@acme.co");

        Supplier persisted = registered.withId("SUP-001");

        assertThat(persisted.id()).isEqualTo("SUP-001");
        assertThat(persisted.name()).isEqualTo(registered.name());
        assertThat(persisted.taxId()).isEqualTo(registered.taxId());
        assertThat(persisted.email()).isEqualTo(registered.email());
    }
}
