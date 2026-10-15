package com.indra.catalog.suppliers.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.indra.catalog.suppliers.domain.Supplier;
import org.junit.jupiter.api.Test;

class InMemorySupplierRepositoryTest {

    private final InMemorySupplierRepository repository = new InMemorySupplierRepository();

    @Test
    void nextId_generatesSequentialFormattedIds() {
        assertThat(repository.nextId()).isEqualTo("SUP-001");
        assertThat(repository.nextId()).isEqualTo("SUP-002");
    }

    @Test
    void save_findAndDelete_roundTrip() {
        Supplier supplier = new Supplier("SUP-001", "ACME", "900123456-7", "compras@acme.co", null);

        repository.save(supplier);
        assertThat(repository.findById("SUP-001")).contains(supplier);
        assertThat(repository.existsById("SUP-001")).isTrue();

        repository.deleteById("SUP-001");
        assertThat(repository.findById("SUP-001")).isEmpty();
    }
}
