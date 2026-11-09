package com.indra.jpa.orders.service;

import com.indra.jpa.orders.model.Order;
import com.indra.jpa.orders.model.OrderStatus;
import com.indra.jpa.orders.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

	private final OrderRepository orderRepository;

	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	// El mapeo a DTO ocurre dentro de la transacción: aquí se materializa el N+1
	@Transactional(readOnly = true)
	public List<OrderView> listByStatus(OrderStatus status) {
		return orderRepository.findByStatusWithItems(status).stream().map(OrderView::from).toList();
	}

	@Transactional(readOnly = true)
	public Page<OrderView> listByStatus(OrderStatus status, Pageable pageable) {
		Page<Order> page = orderRepository.findPageByStatus(status, pageable);
		List<Long> orderIds = page.getContent().stream().map(Order::getId).toList();
		if (!orderIds.isEmpty()) {
			orderRepository.findByIdInWithItemsAndProducts(orderIds);
		}
		return page.map(OrderView::from);
	}
}