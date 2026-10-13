package com.indra.catalog.products.application;

import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.domain.port.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    @DisplayName("Debe encontrar un producto existente por su identificador")
    void findById_existingProduct_returnsProduct() {
        Product product = new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);
        when(productRepository.findById("PRD-0001")).thenReturn(Optional.of(product));

        Product result = productService.findById("PRD-0001");

        assertNotNull(result);
        assertEquals("PRD-0001", result.getId());
        assertEquals("Laptop", result.getName());
        verify(productRepository).findById("PRD-0001");
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException cuando el producto no existe")
    void findById_nonExistingProduct_throwsProductNotFoundException() {
        when(productRepository.findById("PRD-9999")).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.findById("PRD-9999")
        );

        assertTrue(exception.getMessage().contains("Producto no encontrado: PRD-9999"));
        verify(productRepository).findById("PRD-9999");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Debe lanzar IllegalArgumentException si el identificador es nulo o vacio al consultar")
    void findById_nullOrBlankId_throwsIllegalArgumentException(String invalidId) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.findById(invalidId)
        );

        assertTrue(exception.getMessage().contains("El id del producto no puede ser nulo ni vacío"));
    }

    @Test
    @DisplayName("Debe generar id con formato PRD-xxxx al crear un producto sin id")
    void create_validProductWithoutId_generatesIdAndSaves() {
        Product input = new Product(null, "Mouse Ergonomico", new BigDecimal("50.00"), 25);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = productService.create(input);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().startsWith("PRD-"));
        assertEquals("Mouse Ergonomico", created.getName());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();
        assertEquals(created.getId(), saved.getId());
    }

    @Test
    @DisplayName("Debe preservar el id si el producto ya cuenta con uno al crearlo")
    void create_validProductWithExistingId_preservesIdAndSaves() {
        Product input = new Product("PRD-0050", "Teclado Mecanico", new BigDecimal("120.00"), 15);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = productService.create(input);

        assertEquals("PRD-0050", created.getId());
        verify(productRepository).save(input);
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException al intentar crear producto nulo")
    void create_nullProduct_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> productService.create(null));
    }

    @Test
    @DisplayName("Debe eliminar un producto existente correctamente")
    void deleteById_existingProduct_deletesSuccessfully() {
        when(productRepository.existsById("PRD-0001")).thenReturn(true);

        productService.deleteById("PRD-0001");

        verify(productRepository).existsById("PRD-0001");
        verify(productRepository).deleteById("PRD-0001");
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException si se intenta eliminar un producto inexistente")
    void deleteById_nonExistingProduct_throwsProductNotFoundException() {
        when(productRepository.existsById("PRD-9999")).thenReturn(false);

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteById("PRD-9999")
        );

        assertTrue(exception.getMessage().contains("Producto no encontrado: PRD-9999"));
        verify(productRepository).existsById("PRD-9999");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Debe lanzar IllegalArgumentException si el identificador es nulo o vacio al eliminar")
    void deleteById_nullOrBlankId_throwsIllegalArgumentException(String invalidId) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.deleteById(invalidId)
        );

        assertTrue(exception.getMessage().contains("El id del producto no puede ser nulo ni vacío"));
    }
}
