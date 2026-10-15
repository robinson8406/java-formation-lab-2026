package com.indra.catalog.suppliers.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.indra.catalog.suppliers.domain.Supplier;
import org.junit.jupiter.api.Test;

class InMemorySupplierRepositoryTest {

    @Test
    void savesFindsAndDeletesSuppliers() {
        InMemorySupplierRepository repository = new InMemorySupplierRepository();
        Supplier supplier = new Supplier("SUP-001", "Acme", "900123456-7", "compras@acme.co", null);

        repository.save(supplier);

        assertEquals(supplier, repository.findById("SUP-001").orElseThrow());
        assertTrue(repository.deleteById("SUP-001"));
        assertFalse(repository.deleteById("SUP-001"));
    }
}
