package com.indra.catalog.products.application;

import com.indra.catalog.products.domain.model.Product;

public interface ProductService {

    Product findById(String id);

    Product create(Product product);

    void deleteById(String id);
}
