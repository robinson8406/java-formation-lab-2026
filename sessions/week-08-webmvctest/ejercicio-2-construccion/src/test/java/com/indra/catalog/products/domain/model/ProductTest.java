package com.indra.catalog.products.domain.model;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest {

    @Test
    @DisplayName("Debe crear un producto valido y proteger sus invariantes")
    void create_validProduct_initializesCorrectly() {
        Product product = new Product("PRD-0001", "  Laptop Gamer  ", new BigDecimal("3500000.00"), 10);

        assertEquals("PRD-0001", product.getId());
        assertEquals("Laptop Gamer", product.getName());
        assertEquals(new BigDecimal("3500000.00"), product.getPrice());
        assertEquals(10, product.getStock());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Debe lanzar excepcion si el nombre es nulo o vacio")
    void create_blankOrNullName_throwsIllegalArgumentException(String invalidName) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Product("PRD-0001", invalidName, new BigDecimal("100.00"), 5)
        );

        assertTrue(exception.getMessage().contains("El nombre del producto no puede ser nulo ni estar vacío"));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el nombre supera los 120 caracteres")
    void create_nameExceedingMax120Chars_throwsIllegalArgumentException() {
        String longName = "A".repeat(121);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Product("PRD-0001", longName, new BigDecimal("100.00"), 5)
        );

        assertTrue(exception.getMessage().contains("no puede superar los 120 caracteres"));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el precio es nulo, cero o negativo")
    void create_invalidPrice_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product("PRD-0001", "Laptop", null, 5)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product("PRD-0001", "Laptop", BigDecimal.ZERO, 5)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product("PRD-0001", "Laptop", new BigDecimal("-10.00"), 5)
        );
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el stock es nulo o negativo")
    void create_invalidStock_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product("PRD-0001", "Laptop", new BigDecimal("100.00"), null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product("PRD-0001", "Laptop", new BigDecimal("100.00"), -1)
        );
    }

    @Test
    @DisplayName("Debe generar un nuevo producto inmutable al invocar withId")
    void withId_createsNewProductWithNewIdPreservingOtherFields() {
        Product original = new Product(null, "Monitor", new BigDecimal("500.00"), 8);
        Product updated = original.withId("PRD-0002");

        assertNotNull(updated);
        assertEquals("PRD-0002", updated.getId());
        assertEquals("Monitor", updated.getName());
        assertEquals(new BigDecimal("500.00"), updated.getPrice());
        assertEquals(8, updated.getStock());
        assertNotEquals(original, updated);
    }

    @Test
    @DisplayName("Debe implementar equals, hashCode y toString de forma consistente")
    void equalsAndHashCode_consistentForEqualInstances() {
        Product product1 = new Product("PRD-0001", "Teclado", new BigDecimal("150.00"), 20);
        Product product2 = new Product("PRD-0001", "Teclado", new BigDecimal("150.00"), 20);
        Product different = new Product("PRD-0002", "Mouse", new BigDecimal("80.00"), 15);

        assertEquals(product1, product2);
        assertEquals(product1.hashCode(), product2.hashCode());
        assertNotEquals(product1, different);
        assertTrue(product1.toString().contains("Teclado"));
    }
}
