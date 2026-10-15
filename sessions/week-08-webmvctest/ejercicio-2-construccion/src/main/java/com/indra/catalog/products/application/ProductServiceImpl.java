package com.indra.catalog.products.application;

import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.domain.port.ProductRepository;

public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public Product create(CreateProductCommand command) {
        Product product = new Product(productRepository.nextId(), command.name(), command.price(), command.stock());
        return productRepository.save(product);
    }

    @Override
    public void delete(String id) {
        if (!productRepository.deleteById(id)) {
            throw new ProductNotFoundException(id);
        }
    }
}
