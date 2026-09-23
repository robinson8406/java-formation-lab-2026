package co.com.inventory.inventoryservice.usecases.impl;

import co.com.inventory.inventoryservice.models.ProductDto;
import co.com.inventory.inventoryservice.services.IInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryPostUseCaseTest {

	@Mock
	private IInventoryService service;

	@InjectMocks
	private InventoryPostUseCase useCase;

	private ProductDto product;

	@BeforeEach
	void setUp() {
		product = ProductDto.builder().name("Producto A").build();
	}

	@Test
	void create_nullProduct_throwsIllegalArgumentException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.create(null));

		assertEquals("La información del producto esta vacía!", exception.getMessage());
		verifyNoInteractions(service);
	}

	@Test
	void create_productWithoutName_throwsIllegalArgumentException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.create(new ProductDto()));

		assertEquals("El nombre del producto se encuentra vacío!", exception.getMessage());
		verifyNoInteractions(service);
	}

	@Test
	void create_existingId_throwsIllegalStateException() {
		product.setId("1");
		when(service.getById("1")).thenReturn(Collections.singletonList(product));

		IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> useCase.create(product));

		assertEquals("El identificador del producto ya existe!", exception.getMessage());
		verify(service).getById("1");
		verify(service, never()).create(any());
	}

	@Test
	void create_existingName_throwsIllegalStateException() {
		when(service.getByName("Producto A")).thenReturn(Collections.singletonList(product));

		IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> useCase.create(product));

		assertEquals("Ya existe un producto con ese nombre!", exception.getMessage());
		verify(service).getByName("Producto A");
		verify(service, never()).create(any());
	}

	@Test
	void create_uniqueProduct_generatesIdAndDelegatesToService() {
		when(service.getByName("Producto A")).thenReturn(Collections.emptyList());
		when(service.create(any(ProductDto.class))).thenReturn("generated-id");

		String result = useCase.create(product);

		assertEquals("generated-id", result);
		assertNotNull(product.getId());
		verify(service).getByName("Producto A");
		verify(service).create(product);
	}
}