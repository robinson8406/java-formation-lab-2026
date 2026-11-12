package com.indra.jpa.inventory.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.indra.jpa.inventory.model.Product;
import com.indra.jpa.inventory.repository.ProductRepository;

import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = getSpec(name, minPrice, maxPrice);

        return productRepository.searchProducts(spec, pageable);
    }

    @Nonnull
    private static Specification<Product> getSpec(String name, BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase(Locale.ROOT) + "%"));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public Product increaseStock(Long productId, int quantity) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado con id: " + productId));
        product.setStock(Math.addExact(product.getStock(), quantity));

        return productRepository.save(product);
    }

    public void reserveStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado con id: " + productId));

        product.setStock(product.getStock() - quantity);

        try {
            productRepository.saveAndFlush(product);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException exception) {
            throw new ConcurrentModificationException("Stock modificado concurrentemente, reintenta", exception);
        }
    }

}