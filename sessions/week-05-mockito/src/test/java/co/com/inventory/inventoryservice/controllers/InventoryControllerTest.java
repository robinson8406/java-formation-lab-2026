package co.com.inventory.inventoryservice.controllers;

import co.com.inventory.inventoryservice.models.ProductDto;
import co.com.inventory.inventoryservice.usecases.IInventoryGetUseCase;
import co.com.inventory.inventoryservice.usecases.IInventoryPatchUseCase;
import co.com.inventory.inventoryservice.usecases.IInventoryPostUseCase;
import co.com.inventory.inventoryservice.usecases.IInventoryPutUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private IInventoryGetUseCase getUseCase;

    @Mock
    private IInventoryPostUseCase postUseCase;

    @Mock
    private IInventoryPutUseCase putUseCase;

    @Mock
    private IInventoryPatchUseCase patchUseCase;

    @InjectMocks
    private InventoryController controller;

    private ProductDto product;

    @BeforeEach
    void setUp() {
        product = new ProductDto(); // ajusta según los campos reales de tu DTO
    }

    // ----------------------------------------------------------------
    // GET
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("GET /inventory")
    class GetTests {

        @Test
        @DisplayName("Debe retornar 200 OK con datos cuando hay parámetros y el use case devuelve resultados")
        void get_withOptionalsAndData_returnsOk() {
            Map<String, String> optionals = new HashMap<>();
            optionals.put("category", "electronics");

            List<ProductDto> data = List.of(new ProductDto());
            when(getUseCase.read(optionals)).thenReturn(data);

            ResponseEntity<List<ProductDto>> response = controller.get(optionals);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(data, response.getBody());
            verify(getUseCase, times(1)).read(optionals);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando hay parámetros pero el use case devuelve lista vacía")
        void get_withOptionalsButEmptyData_returnsNotFound() {
            Map<String, String> optionals = new HashMap<>();
            optionals.put("category", "electronics");

            when(getUseCase.read(optionals)).thenReturn(Collections.emptyList());

            ResponseEntity<List<ProductDto>> response = controller.get(optionals);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(getUseCase, times(1)).read(optionals);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando hay parámetros pero el use case devuelve null")
        void get_withOptionalsButNullData_returnsNotFound() {
            Map<String, String> optionals = new HashMap<>();
            optionals.put("category", "electronics");

            when(getUseCase.read(optionals)).thenReturn(null);

            ResponseEntity<List<ProductDto>> response = controller.get(optionals);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(getUseCase, times(1)).read(optionals);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el mapa de parámetros está vacío")
        void get_withEmptyOptionals_returnsNotFound() {
            Map<String, String> optionals = new HashMap<>();

            ResponseEntity<List<ProductDto>> response = controller.get(optionals);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(getUseCase, never()).read(anyMap());
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el mapa de parámetros es null")
        void get_withNullOptionals_returnsNotFound() {
            ResponseEntity<List<ProductDto>> response = controller.get(null);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(getUseCase, never()).read(anyMap());
        }
    }

    // ----------------------------------------------------------------
    // POST
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("POST /inventory")
    class PostTests {

        @Test
        @DisplayName("Debe retornar 201 CREATED con el id cuando la creación es exitosa")
        void post_validProduct_returnsCreated() {
            when(postUseCase.create(product)).thenReturn("123");

            ResponseEntity<String> response = controller.post(product);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals("123", response.getBody());
            verify(postUseCase, times(1)).create(product);
        }

        @Test
        @DisplayName("Debe retornar 400 BAD_REQUEST cuando el producto es null")
        void post_nullProduct_returnsBadRequest() {
            ResponseEntity<String> response = controller.post(null);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            // verify(postUseCase, never()).create(any());
        }

        @Test
        @DisplayName("Debe retornar 406 NOT_ACCEPTABLE cuando el use case lanza una excepción")
        void post_useCaseThrowsException_returnsNotAcceptable() {
            when(postUseCase.create(product)).thenThrow(new RuntimeException("error"));

            ResponseEntity<String> response = controller.post(product);

            assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
            verify(postUseCase, times(1)).create(product);
        }
    }

    // ----------------------------------------------------------------
    // PUT
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("PUT /inventory")
    class PutTests {

        @Test
        @DisplayName("Debe retornar 202 ACCEPTED cuando la actualización es exitosa")
        void put_validProduct_returnsAccepted() {
            doNothing().when(putUseCase).update(product);

            ResponseEntity<Void> response = controller.put(product);

            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            verify(putUseCase, times(1)).update(product);
        }

        @Test
        @DisplayName("Debe retornar 400 BAD_REQUEST cuando el producto es null")
        void put_nullProduct_returnsBadRequest() {
            ResponseEntity<Void> response = controller.put(null);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(putUseCase, never()).update(any());
        }

        @Test
        @DisplayName("Debe retornar 406 NOT_ACCEPTABLE cuando el use case lanza una excepción")
        void put_useCaseThrowsException_returnsNotAcceptable() {
            doThrow(new RuntimeException("error")).when(putUseCase).update(product);

            ResponseEntity<Void> response = controller.put(product);

            assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
            verify(putUseCase, times(1)).update(product);
        }
    }

    // ----------------------------------------------------------------
    // PATCH
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("PATCH /inventory/{id}")
    class PatchTests {

        @Test
        @DisplayName("Debe retornar 202 ACCEPTED cuando la actualización parcial es exitosa")
        void patch_validIdAndProduct_returnsAccepted() {
            String id = "abc123";
            doNothing().when(patchUseCase).update(id, product);

            ResponseEntity<String> response = controller.patch(id, product);

            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            verify(patchUseCase, times(1)).update(id, product);
        }

        @Test
        @DisplayName("Debe retornar 404 NOT_FOUND cuando el id es null")
        void patch_nullId_returnsNotFound() {
            ResponseEntity<String> response = controller.patch(null, product);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(patchUseCase, never()).update(anyString(), any());
        }

        @Test
        @DisplayName("Debe retornar 404 NOT_FOUND cuando el id está vacío")
        void patch_emptyId_returnsNotFound() {
            ResponseEntity<String> response = controller.patch("", product);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(patchUseCase, never()).update(anyString(), any());
        }

        @Test
        @DisplayName("Debe retornar 400 BAD_REQUEST cuando el producto es null")
        void patch_nullProduct_returnsBadRequest() {
            ResponseEntity<String> response = controller.patch("abc123", null);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(patchUseCase, never()).update(anyString(), any());
        }

        @Test
        @DisplayName("Debe retornar 406 NOT_ACCEPTABLE cuando el use case lanza una excepción")
        void patch_useCaseThrowsException_returnsNotAcceptable() {
            String id = "abc123";
            doThrow(new RuntimeException("error")).when(patchUseCase).update(id, product);

            ResponseEntity<String> response = controller.patch(id, product);

            assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
            verify(patchUseCase, times(1)).update(id, product);
        }
    }
}