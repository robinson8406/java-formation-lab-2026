package com.indra.jpa.inventory.service;

import java.util.ConcurrentModificationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.indra.jpa.inventory.model.Product;
import com.indra.jpa.inventory.repository.ProductRepository;

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

    public Product increaseStock(Long productId, int quantity) {

        Product product = productRepository.findByIdForUpdate(productId).get();
        product.setStock(Math.addExact(product.getStock(), quantity));
        return productRepository.save(product);
    }

    public void reserveStock(Long productId, int quantity) {

        Product product = productRepository.findById(productId).get();

        product.setStock(product.getStock() - quantity);

        try {
            productRepository.saveAndFlush(product);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException exception) {
            throw new ConcurrentModificationException("Stock modificado concurrentemente, reintenta", exception);
        }
    }

}