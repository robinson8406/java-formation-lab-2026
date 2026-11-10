package com.indra.jpa.inventory.service;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.indra.jpa.inventory.model.Product;
import com.indra.jpa.inventory.repository.ProductRepository;
import com.indra.jpa.inventory.repository.ProductSpecification;

import jakarta.persistence.OptimisticLockException;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String name, Pageable pageable) {
        return productRepository.searchProducts(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> specification = ProductSpecification.withFilters(name, minPrice, maxPrice);
        return productRepository.findAll(specification, pageable);
    }

    public Product increaseStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        product.setStock(Math.addExact(product.getStock(), quantity));
        return productRepository.saveAndFlush(product);
    }

    public void reserveStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        if (product.getStock() < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }

        product.setStock(product.getStock() - quantity);

        try {
            productRepository.saveAndFlush(product);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException exception) {
            throw new ConcurrentModificationException("Stock modificado concurrentemente, reintenta", exception);
        }
    }

}
