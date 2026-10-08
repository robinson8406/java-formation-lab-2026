package com.indra.catalog.products.domain.model;

import java.math.BigDecimal;

/**
 * Producto del catálogo. Inmutable: protege sus invariantes de negocio
 * (nombre, precio y stock) sin depender de Spring ni de la capa web.
 */
public record Product(String id, String name, BigDecimal price, int stock) {

    private static final int MAX_NAME_LENGTH = 120;

    public Product {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("El nombre del producto no puede superar " + MAX_NAME_LENGTH + " caracteres");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio del producto debe ser mayor que 0");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock del producto no puede ser negativo");
        }
    }

    /** Registra un producto nuevo, aún sin id (pendiente de persistir). */
    public static Product register(String name, BigDecimal price, int stock) {
        return new Product(null, name, price, stock);
    }

    public Product withId(String id) {
        return new Product(id, name, price, stock);
    }
}
