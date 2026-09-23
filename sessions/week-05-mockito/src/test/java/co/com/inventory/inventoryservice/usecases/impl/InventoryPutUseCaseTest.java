package co.com.inventory.inventoryservice.usecases.impl;

import co.com.inventory.inventoryservice.models.ProductDto;
import co.com.inventory.inventoryservice.services.IInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.geom.IllegalPathStateException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryPutUseCaseTest {

	@Mock
	private IInventoryService service;

	@InjectMocks
	private InventoryPutUseCase useCase;

	private ProductDto product;

	@BeforeEach
	void setUp() {
		product = ProductDto.builder().id("1").name("Producto A").build();
	}

	@Test
	void update_nullProduct_throwsIllegalArgumentException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.update(null));

		assertEquals("El registro no esta especificado para actualizar!", exception.getMessage());
		verifyNoInteractions(service);
	}

	@Test
	void update_nonExistingProduct_throwsIllegalPathStateException() {
		when(service.getById("1")).thenReturn(Collections.emptyList());

		IllegalPathStateException exception = assertThrows(IllegalPathStateException.class,
				() -> useCase.update(product));

		assertEquals("El registro no existe!", exception.getMessage());
		verify(service).getById("1");
		verify(service, never()).update(product);
	}

	@Test
	void update_existingProduct_delegatesToService() {
		when(service.getById("1")).thenReturn(Collections.singletonList(product));

		useCase.update(product);

		verify(service).getById("1");
		verify(service).update(product);
	}
}