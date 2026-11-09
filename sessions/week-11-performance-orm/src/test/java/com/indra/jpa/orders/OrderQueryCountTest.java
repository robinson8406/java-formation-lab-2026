package com.indra.jpa.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.indra.jpa.orders.config.DataSeeder;
import com.indra.jpa.orders.model.OrderStatus;
import com.indra.jpa.orders.repository.OrderRepository;
import com.indra.jpa.orders.service.OrderService;
import com.indra.jpa.orders.service.OrderView;

import jakarta.persistence.EntityManagerFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class OrderQueryCountTest {

	private static final int ORDERS = 50;

	private final OrderService orderService;
	private final OrderRepository orderRepository;
	private final DataSeeder seeder;
	private final EntityManagerFactory emf;
	private final QueryCounter counter;

	@Autowired
	public OrderQueryCountTest(OrderService orderService,
			OrderRepository orderRepository,
			DataSeeder seeder,
			EntityManagerFactory emf) {
		this.orderService = orderService;
		this.orderRepository = orderRepository;
		this.seeder = seeder;
		this.emf = emf;
		this.counter = new QueryCounter(emf);
	}

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		seeder.seed(ORDERS);
	}

	@Test
	@DisplayName("Debe listar órdenes con sus ítems y nombres de producto correctamente")
	void listsOrdersWithItemsAndProductNames() {
		List<OrderView> orders = orderService.listByStatus(OrderStatus.CONFIRMED);

		assertThat(orders).hasSize(ORDERS);
		assertThat(orders).allSatisfy(order -> {
			assertThat(order.items()).hasSize(DataSeeder.ITEMS_PER_ORDER);
			assertThat(order.items()).allSatisfy(item -> assertThat(item.productName()).startsWith("Producto"));
		});
	}

	@Test
	@DisplayName("Diagnóstico de medición: verifica y reporta el conteo de consultas ejecutadas")
	void diagnosticPrintsQueryCount() {
		var measured = counter.measure(() -> orderService.listByStatus(OrderStatus.CONFIRMED));

		System.out.println(">>> Queries optimizadas para " + ORDERS + " órdenes: " + measured.queries());
		assertThat(measured.queries()).isLessThanOrEqualTo(3);
	}

	@Test
	@DisplayName("Criterio de aceptación: cargar 50 órdenes con sus ítems debe ejecutar como máximo 3 queries")
	void listingOrdersExecutesAtMostThreeQueries() {
		var measured = counter.measure(() -> orderService.listByStatus(OrderStatus.CONFIRMED));

		assertThat(measured.queries()).isLessThanOrEqualTo(3);
	}

	@Test
	@DisplayName("Requisito de paginación: listado paginado no debe generar N+1 (máximo 3 queries)")
	void pagedListingExecutesAtMostThreeQueries() {
		var measured = counter.measure(
				() -> orderService.listByStatus(OrderStatus.CONFIRMED, PageRequest.of(0, 20)));

		assertThat(measured.queries()).isLessThanOrEqualTo(3);
	}

	@Test
	@DisplayName("Bonus: Consulta con JOIN FETCH directo en repositorio ejecuta exactamente 1 query SQL")
	void repositoryJoinFetchExecutesExactlyOneQuery() {
		var measured = counter.measure(() -> orderRepository.findByStatusWithItems(OrderStatus.CONFIRMED));

		assertThat(measured.result()).hasSize(ORDERS);
		assertThat(measured.queries()).isEqualTo(1L);
	}
}
