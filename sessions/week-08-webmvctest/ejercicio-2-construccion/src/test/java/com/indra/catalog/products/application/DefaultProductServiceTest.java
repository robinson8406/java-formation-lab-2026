package com.indra.catalog.products.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.domain.port.ProductIdGenerator;
import com.indra.catalog.products.domain.port.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductIdGenerator idGenerator;

    @InjectMocks
    private DefaultProductService service;

    @Test
    void createAssignsGeneratedIdAndPersistsProduct() {
        when(idGenerator.nextId()).thenReturn("PRD-0001");
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = service.create(new CreateProductCommand("  Laptop ", new BigDecimal("10.00"), 5));

        assertThat(created).isEqualTo(new Product("PRD-0001", "Laptop", new BigDecimal("10.00"), 5));
    }

    @Test
    void findByIdThrowsWhenProductDoesNotExist() {
        when(repository.findById("PRD-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("PRD-9999"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("PRD-9999");
    }

    @Test
    void deleteRemovesExistingProduct() {
        when(repository.existsById("PRD-0001")).thenReturn(true);

        service.delete("PRD-0001");

        verify(repository).deleteById("PRD-0001");
    }

    @Test
    void deleteThrowsAndDoesNotTouchRepositoryWhenProductDoesNotExist() {
        when(repository.existsById("PRD-9999")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("PRD-9999")).isInstanceOf(ProductNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
