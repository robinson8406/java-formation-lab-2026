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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.indra.jpa.orders.config.DataSeeder;
import com.indra.jpa.orders.model.Order;
import com.indra.jpa.orders.model.OrderStatus;
import com.indra.jpa.orders.repository.OrderRepository;
import com.indra.jpa.orders.service.OrderService;
import com.indra.jpa.orders.service.OrderView;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class OrderQueryCountTest {

	private static final int ORDERS = 50;

	private final OrderService orderService;
	private final OrderRepository orderRepository;
	private final DataSeeder seeder;
	private final EntityManagerFactory emf;
	private final EntityManager entityManager;
	private final PlatformTransactionManager transactionManager;
	private final QueryCounter counter;

	@Autowired
	OrderQueryCountTest(OrderService orderService,
			OrderRepository orderRepository,
			DataSeeder seeder,
			EntityManagerFactory emf,
			EntityManager entityManager,
			PlatformTransactionManager transactionManager) {
		this.orderService = orderService;
		this.orderRepository = orderRepository;
		this.seeder = seeder;
		this.emf = emf;
		this.entityManager = entityManager;
		this.transactionManager = transactionManager;
		this.counter = new QueryCounter(emf);
	}

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		seeder.seed(ORDERS);
	}

	@Test
	void listsOrdersWithItemsAndProductNames() {
		var measured = counter.measure(() -> orderService.listByStatus(OrderStatus.CONFIRMED));
		List<OrderView> orders = measured.result();

		assertThat(orders).hasSize(ORDERS);
		assertThat(measured.queries()).isEqualTo(1);
		assertThat(orders).allSatisfy(order -> {
			assertThat(order.items()).hasSize(DataSeeder.ITEMS_PER_ORDER);
			assertThat(order.items()).allSatisfy(item -> assertThat(item.productName()).startsWith("Producto"));
		});
	}

	@Test
	void diagnosticPrintsQueryCount() {
		var measured = counter.measure(() -> orderService.listByStatus(OrderStatus.CONFIRMED));

		System.out.println(">>> Queries después de JOIN FETCH: " + measured.queries());
		assertThat(measured.queries()).isPositive();
	}

	@Test
	void listingOrdersExecutesAtMostThreeQueries() {
		var measured = counter.measure(() -> orderService.listByStatus(OrderStatus.CONFIRMED));

		assertThat(measured.queries()).isLessThanOrEqualTo(3);
	}

	@DisplayName("Reto: el listado paginado tampoco puede tener N+1")
	@Test
	void pagedListingExecutesAtMostThreeQueries() {
		var measured = counter.measure(
				() -> orderService.listByStatus(OrderStatus.CONFIRMED, PageRequest.of(0, 20)));

		assertThat(measured.result().getContent()).hasSize(20);
		assertThat(measured.result().getTotalElements()).isEqualTo(ORDERS);
		assertThat(measured.result().getContent()).allSatisfy(order ->
				assertThat(order.items()).hasSize(DataSeeder.ITEMS_PER_ORDER));
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
