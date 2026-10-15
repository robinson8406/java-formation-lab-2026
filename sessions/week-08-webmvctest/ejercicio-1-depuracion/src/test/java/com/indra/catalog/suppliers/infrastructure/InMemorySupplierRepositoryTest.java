package com.indra.catalog.suppliers.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.indra.catalog.suppliers.domain.Supplier;
import org.junit.jupiter.api.Test;

class InMemorySupplierRepositoryTest {

    private final InMemorySupplierRepository repository = new InMemorySupplierRepository();
    private final Supplier acme = new Supplier("SUP-001", "ACME", "900123456-7", "compras@acme.co", null);

    @Test
    void savedSupplierCanBeFoundAndDeleted() {
        repository.save(acme);

        assertThat(repository.findById("SUP-001")).contains(acme);
        assertThat(repository.existsById("SUP-001")).isTrue();

        repository.deleteById("SUP-001");

        assertThat(repository.findById("SUP-001")).isEmpty();
    }

    @Test
    void idGeneratorProducesSequentialIds() {
        SequentialSupplierIdGenerator generator = new SequentialSupplierIdGenerator();

        assertThat(generator.nextId()).isEqualTo("SUP-001");
        assertThat(generator.nextId()).isEqualTo("SUP-002");
    }
}
