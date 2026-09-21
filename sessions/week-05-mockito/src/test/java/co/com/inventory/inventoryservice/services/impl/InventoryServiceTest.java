package co.com.inventory.inventoryservice.services.impl;

import co.com.inventory.inventoryservice.entities.Product;
import co.com.inventory.inventoryservice.models.ProductDto;
import co.com.inventory.inventoryservice.repositories.ICatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

// PASO 1
// Debes agregar la extension de Mockito
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    // PASO 2
    // Este es el MOCK
    @Mock
    private ICatalogRepository repository;

    // PASO 3
    // Este es la clase que vamos a testear apoyándonos de MOCK
    @InjectMocks
    private InventoryService inventoryService;

    private ProductDto productDto;
    private Product product;

    @BeforeEach
    void setUp() {
        productDto = new ProductDto();
        productDto.setId("1");
        productDto.setName("Producto A");
        // ajusta setters según los campos reales de ProductDto

        product = new Product();
        product.setId("1");
        product.setName("Producto A");
        // ajusta setters según los campos reales de Product
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("Debe retornar el id del producto cuando la creación es exitosa")
        void create_validProduct_returnsId() {
            // PASO 4.1
            // Adicionar el paso WHEN
            when(repository.save(any(Product.class))).thenReturn(product);

            String result = inventoryService.create(productDto);

            assertEquals("1", result);
            verify(repository, times(1)).save(any(Product.class));
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
            assertThrows(IllegalStateException.class,
                    () -> inventoryService.create(null));

            verify(repository, never()).save(any());
        }
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("Debe ejecutar la actualización sin lanzar excepción cuando es exitosa")
        void update_validProduct_completesSuccessfully() {
            when(repository.save(any(Product.class))).thenReturn(product);

            assertDoesNotThrow(() -> inventoryService.update(productDto));

            // PASO 4.2
            // Implementar la sentencia verify para indicar que la actualizacion fue exitosa
            verify(repository, times(1)).save(any(Product.class));
            
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

    // ----------------------------------------------------------------
    // GET BY ID
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("getById")
    class GetByIdTests {

        @Test
        @DisplayName("Debe retornar una lista con un elemento cuando el producto existe")
        void getById_existingId_returnsListWithOneElement() {
            // PASO 4.3
            // Agregar el WHEN
            when(repository.findById("1")).thenReturn(Optional.of(product));
            List<ProductDto> result = inventoryService.getById("1");

            assertEquals(1, result.size());
            assertEquals("1", result.get(0).getId());
            verify(repository, times(1)).findById("1");
        }

        @Test
        @DisplayName("Debe retornar una lista vacía cuando el producto no existe")
        void getById_nonExistingId_returnsEmptyList() {
            when(repository.findById("999")).thenReturn(Optional.empty());

            List<ProductDto> result = inventoryService.getById("999");

            assertTrue(result.isEmpty());
            verify(repository, times(1)).findById("999");
        }
    }

    // ----------------------------------------------------------------
    // GET BY NAME
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("getByName")
    class GetByNameTests {

        @Test
        @DisplayName("Debe retornar la lista mapeada cuando existen resultados")
        void getByName_existingResults_returnsMappedList() {
            when(repository.findByName("Producto A")).thenReturn(Arrays.asList(product));

            List<ProductDto> result = inventoryService.getByName("Producto A");

            assertEquals(1, result.size());
            assertEquals("Producto A", result.get(0).getName());
            verify(repository, times(1)).findByName("Producto A");
        }

        @Test
        @DisplayName("Debe retornar una lista vacía cuando no hay resultados")
        void getByName_noResults_returnsEmptyList() {
            when(repository.findByName("Inexistente")).thenReturn(Collections.emptyList());

            List<ProductDto> result = inventoryService.getByName("Inexistente");

            assertTrue(result.isEmpty());
            verify(repository, times(1)).findByName("Inexistente");
        }
    }

    // ----------------------------------------------------------------
    // GET ALL
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("getAll")
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