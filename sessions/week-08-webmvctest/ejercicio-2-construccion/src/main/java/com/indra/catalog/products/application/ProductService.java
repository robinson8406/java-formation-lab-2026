package com.indra.catalog.products.application;

import com.indra.catalog.products.domain.model.Product;

/** Puerto de entrada: el controller depende de esta abstracción, no de la implementación. */
public interface ProductService {

    Product create(Product product);

    Product findById(String id);

    void delete(String id);
}
