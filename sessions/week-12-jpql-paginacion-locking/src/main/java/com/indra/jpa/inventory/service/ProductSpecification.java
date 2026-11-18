package com.indra.jpa.inventory.service;

import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;
import com.indra.jpa.inventory.model.Product;

public class ProductSpecification {

    public static Specification<Product> hasName(String name) {

        return (root, query, cb) -> {

            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(cb.lower(root.get("name")),"%" + name.toLowerCase() + "%");};
    }

    public static Specification<Product> minPrice(BigDecimal minPrice) {

        return (root, query, cb) -> {

            if (minPrice == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("price"),minPrice);};
    }

    public static Specification<Product> maxPrice(BigDecimal maxPrice) {

        return (root, query, cb) -> {

            if (maxPrice == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("price"),maxPrice);};
    }

}
