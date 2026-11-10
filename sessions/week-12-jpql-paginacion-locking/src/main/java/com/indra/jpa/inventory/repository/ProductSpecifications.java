package com.indra.jpa.inventory.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.indra.jpa.inventory.model.Product;

public class ProductSpecifications {

    public static Specification<Product> hasName(String name) {
        return (root, query, cb)
                -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.like(
                        cb.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );
    }

    public static Specification<Product> stockGreaterThan(int stock) {
        return (root, query, cb)
                -> cb.greaterThan(root.get("stock"), stock);
    }

    public static Specification<Product> priceGreaterThan(BigDecimal price) {
        return (root, query, cb)
                -> price == null
                        ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(
                                root.get("price"),
                                price
                        );
    }

    public static Specification<Product> priceLessThan(BigDecimal price) {
        return (root, query, cb)
                -> price == null
                        ? cb.conjunction()
                        : cb.lessThanOrEqualTo(
                                root.get("price"),
                                price
                        );
    }

}
