package com.indra.catalog.products.application;

import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.domain.port.ProductIdGenerator;
import com.indra.catalog.products.domain.port.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultProductService implements ProductService {

    private final ProductRepository repository;
    private final ProductIdGenerator idGenerator;

    public DefaultProductService(ProductRepository repository, ProductIdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    @Override
    public Product create(CreateProductCommand command) {
        Product product = new Product(idGenerator.nextId(), command.name(), command.price(), command.stock());
        return repository.save(product);
    }

    @Override
    public Product findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
