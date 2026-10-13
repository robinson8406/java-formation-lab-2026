package com.indra.catalog.products.application;

import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.domain.port.ProductRepository;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final AtomicInteger counter = new AtomicInteger(0);

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository, "productRepository no puede ser nulo");
    }

    @Override
    public Product findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del producto no puede ser nulo ni vacío");
        }
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public Product create(Product product) {
        Objects.requireNonNull(product, "El producto no puede ser nulo");
        String id = (product.getId() != null && !product.getId().isBlank())
                ? product.getId()
                : String.format("PRD-%04d", counter.incrementAndGet());
        Product productToSave = product.withId(id);
        return productRepository.save(productToSave);
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del producto no puede ser nulo ni vacío");
        }
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }
}
