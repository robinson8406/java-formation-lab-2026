package com.indra.catalog.products.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.indra.catalog.products.domain.exception.InvalidProductException;
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

    private static final BigDecimal PRICE = new BigDecimal("3500000.00");

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void findById_whenProductExists_returnsIt() {
        Product laptop = new Product("PRD-0001", "Laptop", PRICE, 10);
        when(productRepository.findById("PRD-0001")).thenReturn(Optional.of(laptop));

        assertThat(productService.findById("PRD-0001")).isEqualTo(laptop);
    }

    @Test
    void findById_whenProductDoesNotExist_throwsNotFound() {
        when(productRepository.findById("PRD-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById("PRD-9999"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Producto no encontrado: PRD-9999");
    }

    @Test
    void create_assignsGeneratedIdAndSavesProduct() {
        when(productRepository.nextId()).thenReturn("PRD-0001");
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = productService.create(new CreateProductCommand("Laptop", PRICE, 10));

        assertThat(created).isEqualTo(new Product("PRD-0001", "Laptop", PRICE, 10));
        verify(productRepository).save(created);
    }

    @Test
    void create_withInvalidData_doesNotSave() {
        when(productRepository.nextId()).thenReturn("PRD-0001");
        CreateProductCommand invalid = new CreateProductCommand("Laptop", BigDecimal.ZERO, 10);

        assertThatThrownBy(() -> productService.create(invalid))
                .isInstanceOf(InvalidProductException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_whenProductExists_deletesIt() {
        when(productRepository.deleteById("PRD-0001")).thenReturn(true);

        productService.delete("PRD-0001");

        verify(productRepository).deleteById("PRD-0001");
    }

    @Test
    void delete_whenProductDoesNotExist_throwsNotFound() {
        when(productRepository.deleteById("PRD-9999")).thenReturn(false);

        assertThatThrownBy(() -> productService.delete("PRD-9999"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Producto no encontrado: PRD-9999");
    }
}
