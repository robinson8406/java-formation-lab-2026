package com.indra.catalog.products.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Producto del catálogo. Inmutable: las invariantes se validan una sola vez al construirlo.
 */
public record Product(String id, String name, BigDecimal price, int stock) {

    public Product {
        Objects.requireNonNull(id, "id");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        name = name.trim();
    }
}
