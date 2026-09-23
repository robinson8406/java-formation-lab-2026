package co.com.inventory.inventoryservice.services.impl;

import co.com.inventory.inventoryservice.entities.Product;
import co.com.inventory.inventoryservice.models.ProductDto;
import co.com.inventory.inventoryservice.repositories.ICatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ICatalogRepository repository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @InjectMocks
    private InventoryService inventoryService;

    private ProductDto productDto;
    private Product product;

    @BeforeEach
    void setUp() {
        productDto = new ProductDto();
        productDto.setId("1");
        productDto.setName("Producto A");

        product = new Product();
        product.setId("1");
        product.setName("Producto A");

    }

    @Nested
    @DisplayName("Crear productos")
    class CreateTests {

        @Test
        @DisplayName("Debe retornar el id del producto cuando la creación es exitosa")
        void create_validProduct_returnsId() {
            when(repository.save(any(Product.class))).thenReturn(product);
            String result = inventoryService.create(productDto);

            assertEquals("1", result);
            verify(repository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Debe enviar al repositorio el producto mapeado durante la creación")
        void create_validProduct_savesMappedProduct() {
            when(repository.save(any(Product.class))).thenReturn(product);

            inventoryService.create(productDto);

            verify(repository).save(productCaptor.capture());
            assertEquals("1", productCaptor.getValue().getId());
            assertEquals("Producto A", productCaptor.getValue().getName());
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException cuando el repositorio falla")
        void create_repositoryThrowsException_throwsIllegalStateException() {
            when(repository.save(any(Product.class))).thenThrow(new RuntimeException("db error"));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> inventoryService.create(productDto));

            assertEquals("Proceso de creación NO exitoso!", ex.getMessage());
            verify(repository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException cuando el mapeo falla (productDto null)")
        void create_nullProductDto_throwsIllegalStateException() {
            assertThrows(IllegalStateException.class, () -> inventoryService.create(null));
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Actualizar productos")
    class UpdateTests {

        @Test
        @DisplayName("Debe ejecutar la actualización sin lanzar excepción cuando es exitosa")
        void update_validProduct_completesSuccessfully() {
            when(repository.save(any(Product.class))).thenReturn(product);
            assertDoesNotThrow(() -> inventoryService.update(productDto));

            verify(repository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Debe enviar al repositorio el producto mapeado durante la actualización")
        void update_validProduct_savesMappedProduct() {
            when(repository.save(any(Product.class))).thenReturn(product);

            inventoryService.update(productDto);

            verify(repository).save(productCaptor.capture());
            assertEquals("1", productCaptor.getValue().getId());
            assertEquals("Producto A", productCaptor.getValue().getName());
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException cuando el repositorio falla")
        void update_repositoryThrowsException_throwsIllegalStateException() {
            when(repository.save(any(Product.class))).thenThrow(new RuntimeException("db error"));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> inventoryService.update(productDto));

            assertEquals("Proceso de actualizacion NO exitoso!", ex.getMessage());
            verify(repository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException cuando el mapeo falla (productDto null)")
        void update_nullProductDto_throwsIllegalStateException() {
            assertThrows(IllegalStateException.class,
                    () -> inventoryService.update(null));

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Buscar productos por identificador")
    class GetByIdTests {

        @Test
        @DisplayName("Debe retornar una lista con un elemento cuando el producto existe")
        void getById_existingId_returnsListWithOneElement() {
            when(repository.findById(anyString())).thenReturn(Optional.of(product));
            List<ProductDto> result = inventoryService.getById("1");
            assertEquals(1, result.size());
            assertEquals("1", result.getFirst().getId());
            verify(repository, times(1)).findById("1");
        }

        @ParameterizedTest
        @CsvSource({"999", "abc", "no-existe"})
        @DisplayName("Debe retornar una lista vacía cuando el producto no existe")
        void getById_nonExistingId_returnsEmptyList(String id) {
            when(repository.findById(anyString())).thenReturn(Optional.empty());

            List<ProductDto> result = inventoryService.getById(id);

            assertTrue(result.isEmpty());
            verify(repository).findById(id);
        }
    }

    @Nested
    @DisplayName("Buscar productos por nombre")
    class GetByNameTests {

        @Test
        @DisplayName("Debe retornar la lista mapeada cuando existen resultados")
        void getByName_existingResults_returnsMappedList() {
            when(repository.findByName("Producto A")).thenReturn(Arrays.asList(product));

            List<ProductDto> result = inventoryService.getByName("Producto A");

            assertEquals(1, result.size());
            assertEquals("Producto A", result.getFirst().getName());
            verify(repository, times(1)).findByName("Producto A");
        }

        @ParameterizedTest
        @ValueSource(strings = {"Inexistente", "Sin resultados", "Producto X"})
        @DisplayName("Debe retornar una lista vacía cuando no hay resultados")
        void getByName_noResults_returnsEmptyList(String name) {
            when(repository.findByName(anyString())).thenReturn(Collections.emptyList());

            List<ProductDto> result = inventoryService.getByName(name);

            assertTrue(result.isEmpty());
            verify(repository).findByName(name);
        }
    }

    @Nested
    @DisplayName("Consultar todos los productos")
    class GetAllTests {

        @Test
        @DisplayName("Debe retornar la lista completa mapeada cuando hay productos")
        void getAll_withResults_returnsMappedList() {
            Product product2 = new Product();
            product2.setId("2");
            product2.setName("Producto B");

            when(repository.findAll()).thenReturn(Arrays.asList(product, product2));

            List<ProductDto> result = inventoryService.getAll();

            assertEquals(2, result.size());
            verify(repository, times(1)).findAll();
        }

        @Test
        @DisplayName("Debe retornar una lista vacía cuando no hay productos")
        void getAll_noResults_returnsEmptyList() {
            when(repository.findAll()).thenReturn(Collections.emptyList());

            List<ProductDto> result = inventoryService.getAll();

            assertTrue(result.isEmpty());
            verify(repository, times(1)).findAll();
        }
    }
}