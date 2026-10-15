package com.indra.catalog.products.domain.port;

import com.indra.catalog.products.domain.model.Product;
import java.util.Optional;

/** Puerto de salida: el dominio define qué necesita, la infraestructura decide cómo. */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(String id);

    boolean existsById(String id);

    void deleteById(String id);
}
