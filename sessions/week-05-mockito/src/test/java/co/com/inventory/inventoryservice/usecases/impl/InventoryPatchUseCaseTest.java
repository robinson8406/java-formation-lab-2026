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
class InventoryPatchUseCaseTest {

	@Mock
	private IInventoryService service;

	@InjectMocks
	private InventoryPatchUseCase useCase;

	private ProductDto existingProduct;

	@BeforeEach
	void setUp() {
		existingProduct = ProductDto.builder()
				.id("1")
				.name("Producto A")
				.description("Original")
				.units("unidad")
				.quantity(2.0)
				.build();
	}

	@Test
	void update_missingIdOrProduct_throwsIllegalArgumentException() {
		ProductDto changes = ProductDto.builder().name("Nuevo nombre").build();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.update(null, changes));

		assertEquals("El registro no esta especificado para actualizar!", exception.getMessage());
		verifyNoInteractions(service);
	}

	@Test
	void update_nonExistingProduct_throwsIllegalPathStateException() {
		when(service.getById("1")).thenReturn(Collections.emptyList());

		IllegalPathStateException exception = assertThrows(IllegalPathStateException.class,
				() -> useCase.update("1", new ProductDto()));

		assertEquals("El registro no existe!", exception.getMessage());
		verify(service).getById("1");
		verify(service, never()).update(any());
	}

	@Test
	void update_partialProduct_mergesProvidedFieldsAndDelegatesToService() {
		ProductDto changes = ProductDto.builder().name("Producto actualizado").quantity(5.0).build();
		when(service.getById("1")).thenReturn(Collections.singletonList(existingProduct));

		useCase.update("1", changes);

		assertEquals("Producto actualizado", existingProduct.getName());
		assertEquals(5.0, existingProduct.getQuantity());
		assertEquals("Original", existingProduct.getDescription());
		assertEquals("unidad", existingProduct.getUnits());
		verify(service).getById("1");
		verify(service).update(existingProduct);
	}
}