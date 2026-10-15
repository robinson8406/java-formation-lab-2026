package com.indra.catalog.products.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.domain.port.ProductRepository;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createsProductsWithGeneratedIdsAndDeletesExistingProducts() {
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
        Product created = productService.create("Laptop", new BigDecimal("3500000.00"), 10);
        when(repository.deleteById(created.id())).thenReturn(true);

        assertEquals("PRD-0001", created.id());
        productService.delete(created.id());
        verify(repository).deleteById("PRD-0001");
    }

    @Test
    void throwsNotFoundWhenProductDoesNotExist() {
        when(repository.findById("PRD-9999")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById("PRD-9999"));
    }
}
