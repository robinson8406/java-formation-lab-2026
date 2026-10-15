package com.indra.catalog.products.domain.model;

import com.indra.catalog.products.domain.exception.InvalidProductException;
import java.math.BigDecimal;

public record Product(String id, String name, BigDecimal price, int stock) {
    public static final int MAX_NAME_LENGTH = 120;

    public Product {
        if (id == null || id.isBlank()) {
            throw new InvalidProductException("el id es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("el nombre es obligatorio");
        }
        name = name.trim();
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidProductException("el nombre no puede superar " + MAX_NAME_LENGTH + " caracteres");
        }
        if (price == null || price.signum() <= 0) {
            throw new InvalidProductException("el precio debe ser mayor que 0");
        }
        if (stock < 0) {
            throw new InvalidProductException("el stock no puede ser negativo");
        }
    }
}
