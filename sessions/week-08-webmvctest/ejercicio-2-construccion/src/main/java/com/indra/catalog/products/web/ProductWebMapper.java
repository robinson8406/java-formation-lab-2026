package com.indra.catalog.products.web;

import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.web.dto.CreateProductRequest;
import com.indra.catalog.products.web.dto.ProductResponse;

/**
 * Traductor HTTP puro: no tiene estado ni dependencias, por lo que no necesita
 * ser un bean de Spring ni cargarse en el slice {@code @WebMvcTest}.
 */
final class ProductWebMapper {

    private ProductWebMapper() {
    }

    static Product toDomain(CreateProductRequest request) {
        return Product.register(request.name(), request.price(), request.stock());
    }

    static ProductResponse toResponse(Product product) {
        return new ProductResponse(product.id(), product.name(), product.price(), product.stock());
    }
}
