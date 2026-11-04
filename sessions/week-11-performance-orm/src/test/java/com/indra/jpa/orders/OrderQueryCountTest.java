package com.indra.jpa.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quickperf.sql.annotation.ExpectMaxSelect;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
		assertThat(orders).allSatisfy(order -> {
			assertThat(order.items()).hasSize(DataSeeder.ITEMS_PER_ORDER);
			assertThat(order.items()).allSatisfy(item -> assertThat(item.productName()).startsWith("Producto"));
		});
	}

	@Test
    @ExpectSelect(1) 
	void listingOrdersExecutesAtMostThreeQueries() {
		var measured = counter.measure(() -> orderService.listByStatus(OrderStatus.CONFIRMED));

		System.out.println("Queries totals for get Orders with JOIN FETCH="+ measured.queries());
		assertThat(measured.queries()).isLessThanOrEqualTo(3);
	}

	@Test
    @ExpectMaxSelect(1) 
	void listingOrdersExecutesAtMostThreeQueriesOtherWay() {
		var measured = counter.measure(() -> orderService.listByStatusOtherWay(OrderStatus.CONFIRMED));
		System.out.println("Queries totals for get Orders with EntityGraph="+ measured.queries());
		assertThat(measured.queries()).isLessThanOrEqualTo(1);
	}

	@Test
    @ExpectMaxSelect(3)
	void pagedListingExecutesAtMostThreeQueries() {
		var measured = counter.measure(
				() -> orderService.listByStatus(OrderStatus.CONFIRMED, org.springframework.data.domain.PageRequest.of(0, 20)));

		System.out.println("Queries totals for get Orders Pageable="+ measured.queries());
		assertThat(measured.queries()).isLessThanOrEqualTo(3);
	}
}