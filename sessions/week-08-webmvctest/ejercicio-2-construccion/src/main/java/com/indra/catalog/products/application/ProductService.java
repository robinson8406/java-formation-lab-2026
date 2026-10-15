package com.indra.catalog.products.application;

import com.indra.catalog.products.domain.model.Product;

public interface ProductService {
    
    Product findById(String id);

    Product create(CreateProductCommand command);

    void delete(String id);
}
