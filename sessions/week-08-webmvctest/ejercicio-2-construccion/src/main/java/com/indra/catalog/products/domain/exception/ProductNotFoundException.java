package com.indra.catalog.products.domain.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String productId) {
        super("Producto no encontrado: " + productId);
    }
}
