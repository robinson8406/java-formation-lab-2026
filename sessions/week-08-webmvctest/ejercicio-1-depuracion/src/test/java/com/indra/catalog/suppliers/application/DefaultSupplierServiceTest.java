package com.indra.catalog.suppliers.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierIdGenerator;
import com.indra.catalog.suppliers.domain.SupplierNotFoundException;
import com.indra.catalog.suppliers.domain.SupplierRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultSupplierServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-08T15:00:00Z"), ZoneOffset.UTC);

    private final SupplierRepository repository = mock(SupplierRepository.class);
    private final SupplierIdGenerator idGenerator = mock(SupplierIdGenerator.class);
    private final DefaultSupplierService service = new DefaultSupplierService(repository, idGenerator, FIXED_CLOCK);

    @Test
    void createAssignsGeneratedIdNormalizesDataAndRecordsCreationDate() {
        when(idGenerator.nextId()).thenReturn("SUP-001");
        when(repository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Supplier created = service.create(new CreateSupplierCommand("  acme ltda ", "900.123.456-7", "compras@acme.co"));

        assertThat(created).isEqualTo(new Supplier("SUP-001", "ACME LTDA", "900123456-7", "compras@acme.co",
                "Creado vía API el 2026-10-08T15:00:00Z"));
    }

    @Test
    void findByIdThrowsWhenSupplierDoesNotExist() {
        when(repository.findById("SUP-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("SUP-999"))
                .isInstanceOf(SupplierNotFoundException.class)
                .hasMessageContaining("SUP-999");
    }

    @Test
    void deleteRemovesExistingSupplier() {
        when(repository.existsById("SUP-001")).thenReturn(true);

        service.delete("SUP-001");

        verify(repository).deleteById("SUP-001");
    }

    @Test
    void deleteThrowsAndDoesNotTouchRepositoryWhenSupplierDoesNotExist() {
        when(repository.existsById("SUP-999")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("SUP-999")).isInstanceOf(SupplierNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
