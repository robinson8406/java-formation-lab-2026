package com.indra.jpa.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

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

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private DataSeeder seeder;

	@Autowired
	private EntityManagerFactory emf;

	@Autowired
	private TransactionTemplate transactionTemplate;

	private QueryCounter counter;

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		seeder.seed(ORDERS);
		counter = new QueryCounter(emf);
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

	// Diagnóstico de la alternativa LAZY con batch fetching (seeds 50: 1 + 2 + 6 = 9 queries).
	@Test
	void diagnosticPrintsQueryCount() {
		var measured = counter.measure(() -> transactionTemplate.execute(status ->
				orderRepository.findByStatus(OrderStatus.CONFIRMED).stream().map(OrderView::from).toList()));

		System.out.println(">>> Queries para " + ORDERS + " órdenes: " + measured.queries());
		assertThat(measured.queries()).isEqualTo(9);
	}

	@Test
	void listingOrdersExecutesAtMostThreeQueries() {
		var measured = counter.measure(() -> orderService.listByStatus(OrderStatus.CONFIRMED));

		assertThat(measured.result()).hasSize(ORDERS);
		assertThat(measured.queries()).isLessThanOrEqualTo(3);
	}

	@Test
	void pagedListingExecutesAtMostThreeQueries() {
		var measured = counter.measure(
				() -> orderService.listByStatus(OrderStatus.CONFIRMED, PageRequest.of(0, 20)));

		assertThat(measured.result().getContent()).hasSize(20);
		assertThat(measured.result().getTotalElements()).isEqualTo(ORDERS);
		assertThat(measured.queries()).isLessThanOrEqualTo(3);
	}

	@Test
	void entityGraphLoadsOrdersItemsAndProductsInOneQuery() {
		var measured = counter.measure(() -> orderRepository.findByStatusOrderByIdAsc(OrderStatus.CONFIRMED).stream()
				.map(OrderView::from).toList());

		assertThat(measured.result()).hasSize(ORDERS);
		assertThat(measured.queries()).isEqualTo(1);
	}
}