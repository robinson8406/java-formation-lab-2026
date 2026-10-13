package com.indra.catalog.products.domain.exception;

public class ProductNotFoundException extends RuntimeException {

    private final String productId;

    public ProductNotFoundException(String productId) {
        super("Producto no encontrado: " + productId);
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
