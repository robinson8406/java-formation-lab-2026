package com.indra.catalog.products.infrastructure.persistence;

import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.domain.port.ProductRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> products = new ConcurrentHashMap<>();

    @Override
    public Product save(Product product) {
        products.put(product.id(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return products.containsKey(id);
    }

    @Override
    public void deleteById(String id) {
        products.remove(id);
    }
}
