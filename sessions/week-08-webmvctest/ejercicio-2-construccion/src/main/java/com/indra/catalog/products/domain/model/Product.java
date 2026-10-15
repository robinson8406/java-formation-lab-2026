package com.indra.catalog.products.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Product(String id, String name, BigDecimal price, int stock) {

    public Product {
        name = Objects.requireNonNull(name, "name").strip();
        price = Objects.requireNonNull(price, "price");
        if (id != null && id.isBlank()) {
            throw new IllegalArgumentException("El identificador no puede estar vacío");
        }
        if (name.isBlank() || name.length() > 120) {
            throw new IllegalArgumentException("El nombre debe tener entre 1 y 120 caracteres");
        }
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El inventario no puede ser negativo");
        }
    }

    public Product withId(String productId) {
        return new Product(productId, name, price, stock);
    }
}
