package com.indra.catalog.products.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Unitario puro (sin Spring): el dominio protege sus invariantes. */
class ProductTest {

    @Test
    void register_conDatosValidos_creaElProductoSinId() {
        Product product = Product.register("Laptop", new BigDecimal("3500000.00"), 10);

        assertThat(product.id()).isNull();
        assertThat(product.name()).isEqualTo("Laptop");
        assertThat(product.price()).isEqualByComparingTo("3500000.00");
        assertThat(product.stock()).isEqualTo(10);
    }

    @Test
    void register_conNombreVacio_lanzaExcepcion() {
        assertThatThrownBy(() -> Product.register(" ", BigDecimal.TEN, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_conPrecioMenorOIgualACero_lanzaExcepcion() {
        assertThatThrownBy(() -> Product.register("Laptop", BigDecimal.ZERO, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_conStockNegativo_lanzaExcepcion() {
        assertThatThrownBy(() -> Product.register("Laptop", BigDecimal.TEN, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withId_conservaLosDemasDatos() {
        Product registered = Product.register("Laptop", BigDecimal.TEN, 5);

        Product persisted = registered.withId("PRD-0001");

        assertThat(persisted.id()).isEqualTo("PRD-0001");
        assertThat(persisted.name()).isEqualTo(registered.name());
        assertThat(persisted.price()).isEqualByComparingTo(registered.price());
        assertThat(persisted.stock()).isEqualTo(registered.stock());
    }
}
