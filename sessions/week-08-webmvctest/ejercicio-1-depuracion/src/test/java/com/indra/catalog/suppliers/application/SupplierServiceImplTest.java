package com.indra.catalog.suppliers.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierNotFoundException;
import com.indra.catalog.suppliers.domain.SupplierRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository repository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Test
    void createsSupplierWithGeneratedIdAndDeletesIt() {
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
        Supplier created = supplierService.create(
                new Supplier(null, "Acme Ltda", "900123456-7", "compras@acme.co", null));
        when(repository.deleteById(created.id())).thenReturn(true);

        assertEquals("SUP-001", created.id());
        supplierService.delete(created.id());
        verify(repository).deleteById("SUP-001");
    }

    @Test
    void throwsNotFoundWhenSupplierDoesNotExist() {
        when(repository.findById("SUP-999")).thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class, () -> supplierService.findById("SUP-999"));
    }
}
