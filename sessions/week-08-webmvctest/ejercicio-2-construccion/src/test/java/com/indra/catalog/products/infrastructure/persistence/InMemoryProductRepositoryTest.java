package com.indra.catalog.products.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.indra.catalog.products.domain.model.Product;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class InMemoryProductRepositoryTest {

    @Test
    void savesFindsAndDeletesProducts() {
        InMemoryProductRepository repository = new InMemoryProductRepository();
        Product product = new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);

        repository.save(product);

        assertEquals(product, repository.findById("PRD-0001").orElseThrow());
        assertTrue(repository.deleteById("PRD-0001"));
        assertFalse(repository.deleteById("PRD-0001"));
    }
}
