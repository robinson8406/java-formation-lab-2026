package com.indra.catalog.products.infrastructure.persistence;

import com.indra.catalog.products.domain.model.Product;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryProductRepositoryTest {

    private InMemoryProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepository();
    }

    @Test
    @DisplayName("Debe guardar y consultar un producto en memoria correctamente")
    void saveAndFindById_productStored_returnsSavedProduct() {
        Product product = new Product("PRD-0001", "Laptop", new BigDecimal("2500.00"), 5);

        Product saved = repository.save(product);
        Optional<Product> found = repository.findById("PRD-0001");

        assertNotNull(saved);
        assertTrue(found.isPresent());
        assertEquals("Laptop", found.get().getName());
        assertEquals(new BigDecimal("2500.00"), found.get().getPrice());
    }

    @Test
    @DisplayName("Debe retornar vacio al consultar un producto inexistente")
    void findById_nonExisting_returnsEmptyOptional() {
        Optional<Product> found = repository.findById("PRD-9999");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Debe verificar existencia de un producto correctamente")
    void existsById_returnsCorrectBoolean() {
        Product product = new Product("PRD-0001", "Laptop", new BigDecimal("2500.00"), 5);
        repository.save(product);

        assertTrue(repository.existsById("PRD-0001"));
        assertFalse(repository.existsById("PRD-9999"));
    }

    @Test
    @DisplayName("Debe eliminar un producto de memoria correctamente")
    void deleteById_removesProductFromStorage() {
        Product product = new Product("PRD-0001", "Laptop", new BigDecimal("2500.00"), 5);
        repository.save(product);

        repository.deleteById("PRD-0001");

        assertFalse(repository.existsById("PRD-0001"));
        assertFalse(repository.findById("PRD-0001").isPresent());
    }
}
