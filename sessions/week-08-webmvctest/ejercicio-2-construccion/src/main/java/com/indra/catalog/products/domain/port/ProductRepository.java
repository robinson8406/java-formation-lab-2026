package com.indra.catalog.products.domain.port;

import com.indra.catalog.products.domain.model.Product;
import java.util.Optional;

public interface ProductRepository {
    
    String nextId();

    Product save(Product product);

    Optional<Product> findById(String id);

    /**
     * @return {@code true} si el producto existía y fue eliminado
     */
    boolean deleteById(String id);
}
