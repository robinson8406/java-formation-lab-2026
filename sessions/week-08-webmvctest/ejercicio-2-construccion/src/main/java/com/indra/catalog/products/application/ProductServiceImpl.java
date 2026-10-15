package com.indra.catalog.products.application;

import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.domain.port.ProductRepository;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final AtomicLong sequence = new AtomicLong();

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product findById(String id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public Product create(String name, BigDecimal price, int stock) {
        Product product = new Product(null, name, price, stock)
                .withId("PRD-%04d".formatted(sequence.incrementAndGet()));
        return repository.save(product);
    }

    @Override
    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new ProductNotFoundException(id);
        }
    }
}
