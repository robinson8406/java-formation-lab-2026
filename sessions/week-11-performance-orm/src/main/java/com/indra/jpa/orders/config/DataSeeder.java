package com.indra.jpa.orders.config;

import com.indra.jpa.orders.model.Order;
import com.indra.jpa.orders.model.OrderItem;
import com.indra.jpa.orders.model.OrderStatus;
import com.indra.jpa.orders.model.Product;
import com.indra.jpa.orders.repository.OrderRepository;
import com.indra.jpa.orders.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Dataset determinista: cada orden tiene 3 ítems y cada ítem un producto distinto,
 * por eso 100 órdenes producen 1 + 100 + 300 = 401 queries con el mapeo LAZY actual.
 */
@Component
public class DataSeeder {

	public static final int ITEMS_PER_ORDER = 3;

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;

	public DataSeeder(OrderRepository orderRepository, ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
	}

	@Transactional
	public void seed(int orders) {
		for (int i = 1; i <= orders; i++) {
			Order order = new Order("Cliente " + i, OrderStatus.CONFIRMED);
			for (int j = 1; j <= ITEMS_PER_ORDER; j++) {
				Product product = productRepository
						.save(new Product("Producto " + i + "-" + j, BigDecimal.valueOf(10L * j)));
				order.addItem(new OrderItem(product, j));
			}
			orderRepository.save(order);
		}
	}
}