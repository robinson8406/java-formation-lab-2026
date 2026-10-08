package com.indra.catalog.suppliers.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierNotFoundException;
import com.indra.catalog.suppliers.domain.SupplierRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unitario puro (sin Spring): verifica la asignación de id y el DIP sobre {@link SupplierRepository}. */
class SupplierServiceImplTest {

    private final SupplierRepository repository = mock(SupplierRepository.class);
    private SupplierServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SupplierServiceImpl(repository);
    }

    @Test
    void create_asignaUnIdSecuencialYPersisteElProveedor() {
        Supplier toRegister = Supplier.register("ACME LTDA", "900123456-7", "compras@acme.co");
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Supplier created = service.create(toRegister);

        assertThat(created.id()).isEqualTo("SUP-001");
        verify(repository).save(created);
    }

    @Test
    void delete_conProveedorInexistente_lanzaSupplierNotFoundException() {
        when(repository.existsById("SUP-999")).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(SupplierNotFoundException.class,
                () -> service.delete("SUP-999"));
    }

    @Test
    void findById_conProveedorExistente_loDevuelve() {
        Supplier supplier = new Supplier("SUP-001", "ACME LTDA", "900123456-7", "compras@acme.co", "nota");
        when(repository.findById("SUP-001")).thenReturn(Optional.of(supplier));

        assertThat(service.findById("SUP-001")).isEqualTo(supplier);
    }
}
