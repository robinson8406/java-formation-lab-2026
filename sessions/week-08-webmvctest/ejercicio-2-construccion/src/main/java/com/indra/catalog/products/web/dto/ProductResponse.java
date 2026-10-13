package com.indra.catalog.products.web.dto;

import com.indra.catalog.products.domain.model.Product;
import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        BigDecimal price,
        Integer stock
) {

    public static ProductResponse fromDomain(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }
}
