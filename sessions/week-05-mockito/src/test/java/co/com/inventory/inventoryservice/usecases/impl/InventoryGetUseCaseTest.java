package co.com.inventory.inventoryservice.usecases.impl;

import co.com.inventory.inventoryservice.models.ProductDto;
import co.com.inventory.inventoryservice.services.IInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryGetUseCaseTest {

	@Mock
	private IInventoryService service;

	private InventoryGetUseCase useCase;
	private ProductDto product;

	@BeforeEach
	void setUp() {
		useCase = new InventoryGetUseCase(service);
		product = ProductDto.builder().id("1").name("Producto A").build();
	}

	@Test
	void read_nullPathVariables_returnsEmptyList() {
		assertTrue(useCase.read(null).isEmpty());

		verifyNoInteractions(service);
	}

	@Test
	void read_idPathVariable_delegatesToGetById() {
		when(service.getById("1")).thenReturn(List.of(product));

		List<ProductDto> result = useCase.read(Map.of("id", "1"));

		assertEquals(List.of(product), result);
		verify(service).getById("1");
	}

	@Test
	void read_namePathVariable_delegatesToGetByName() {
		when(service.getByName("Producto A")).thenReturn(List.of(product));

		List<ProductDto> result = useCase.read(Map.of("name", "Producto A"));

		assertEquals(List.of(product), result);
		verify(service).getByName("Producto A");
	}

	@Test
	void read_otherPathVariable_delegatesToGetAll() {
		when(service.getAll()).thenReturn(Collections.singletonList(product));

		List<ProductDto> result = useCase.read(Map.of("filter", "all"));

		assertEquals(List.of(product), result);
		verify(service).getAll();
	}
}