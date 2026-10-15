package com.indra.catalog.products.application;

import com.indra.catalog.products.domain.model.Product;

import java.math.BigDecimal;

public interface ProductService {

    Product findById(String id);

    Product create(String name, BigDecimal price, int stock);

    void delete(String id);
}
