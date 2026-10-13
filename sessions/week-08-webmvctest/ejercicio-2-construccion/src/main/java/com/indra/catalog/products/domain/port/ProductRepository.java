package com.indra.catalog.products.domain.port;

import com.indra.catalog.products.domain.model.Product;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(String id);

    void deleteById(String id);

    boolean existsById(String id);
}
