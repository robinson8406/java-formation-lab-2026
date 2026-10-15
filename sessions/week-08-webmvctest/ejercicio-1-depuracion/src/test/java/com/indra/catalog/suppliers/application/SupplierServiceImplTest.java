package com.indra.catalog.suppliers.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierNotFoundException;
import com.indra.catalog.suppliers.infrastructure.InMemorySupplierRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SupplierServiceImplTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-15T14:30:00Z"), ZoneOffset.UTC);

    private InMemorySupplierRepository repository;
    private SupplierService service;

    @BeforeEach
    void setUp() {
        repository = new InMemorySupplierRepository();
        service = new SupplierServiceImpl(repository, FIXED_CLOCK);
    }

    @Test
    void create_assignsSequentialIdNormalizesAndStampsInternalNote() {
        Supplier first = service.create(new CreateSupplierCommand("  acme ltda ", "900.123.456-7", "compras@acme.co"));
        Supplier second = service.create(new CreateSupplierCommand("Globex", "800123456-1", "info@globex.co"));

        assertThat(first.id()).isEqualTo("SUP-001");
        assertThat(second.id()).isEqualTo("SUP-002");
        assertThat(first.name()).isEqualTo("ACME LTDA");
        assertThat(first.taxId()).isEqualTo("900123456-7");
        assertThat(first.internalNotes()).isEqualTo("Creado vía API el 2026-10-15T14:30");
        assertThat(repository.findById("SUP-001")).contains(first);
    }

    @Test
    void findById_whenMissing_throwsNotFound() {
        assertThatThrownBy(() -> service.findById("SUP-999"))
                .isInstanceOf(SupplierNotFoundException.class)
                .hasMessageContaining("SUP-999");
    }

    @Test
    void delete_existingSupplier_removesIt() {
        Supplier created = service.create(new CreateSupplierCommand("ACME", "900123456-7", "compras@acme.co"));

        service.delete(created.id());

        assertThat(repository.existsById(created.id())).isFalse();
    }

    @Test
    void delete_whenMissing_throwsNotFound() {
        assertThatThrownBy(() -> service.delete("SUP-999"))
                .isInstanceOf(SupplierNotFoundException.class);
    }
}
