package com.indra.jpa.orders;

import com.indra.jpa.orders.config.DataSeeder;
import com.indra.jpa.orders.model.OrderStatus;
import com.indra.jpa.orders.repository.OrderRepository;
import com.indra.jpa.orders.service.OrderService;
import com.indra.jpa.orders.service.OrderView;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class OrderQueryCountTest {

	private static final int ORDERS = 50;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private DataSeeder seeder;

	@Autowired
	private EntityManagerFactory emf;

	private QueryCounter counter;

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		seeder.seed(ORDERS);
		counter = new QueryCounter(emf);
	}

	@Test
	void listsOrdersWithItemsAndProductNames() {
		List<OrderView> orders = orderService.listByStatus(OrderStatus.CONFIRMED);

		assertThat(orders).hasSize(ORDERS);
	}

	// Diagnóstico para la demo: muestra el N+1 (1 + N órdenes + N*3 productos). No lo borres antes del fix.
	@Test
	void diagnosticPrintsQueryCount() {
		var measured = counter.measure(() -> orderService.listByStatus(OrderStatus.CONFIRMED));

		assertThat(measured.queries()).isPositive();
	}

	// Antes: 201 queries.
	// Después del fix: 1 query.
	@Test
	void listingOrdersExecutesAtMostThreeQueries() {
		var measured = counter.measure(() -> orderService.listByStatus(OrderStatus.CONFIRMED));

		assertThat(measured.queries()).isLessThanOrEqualTo(3);
	}

	// Antes: 82 queries.
	// Después del fix: 3 queries.
	@Test
	void pagedListingExecutesAtMostThreeQueries() {
		var measured = counter.measure(
				() -> orderService.listByStatus(OrderStatus.CONFIRMED, org.springframework.data.domain.PageRequest.of(0, 20)));

		assertThat(measured.queries()).isLessThanOrEqualTo(3);
	}

	@Test
	void loadingFiftyOrdersGenerateMaxThreeQueries() {
		var measured = counter.measure(() -> orderService.listByStatus(OrderStatus.CONFIRMED));

		assertThat(measured.result()).hasSize(ORDERS);
		assertThat(measured.result()).allSatisfy(order -> {
			assertThat(order.items()).hasSize(DataSeeder.ITEMS_PER_ORDER);
			assertThat(order.items()).allSatisfy(item -> assertThat(item.productName()).startsWith("Producto"));
		});
		assertThat(measured.queries())
				.as("Consultas SQL al cargar %s ordenes con items y productos", ORDERS)
				.isBetween(1L, 3L);
	}

	@Test
	void pagedListingKeepsOrderTotalAndSize() {
		var page = orderService.listByStatus(OrderStatus.CONFIRMED, PageRequest.of(1, 20));

		assertThat(page.getContent()).hasSize(20);
		assertThat(page.getTotalElements()).isEqualTo(ORDERS);
		assertThat(page.getContent().getFirst().customerName()).isEqualTo("Cliente 21");
	}

}
