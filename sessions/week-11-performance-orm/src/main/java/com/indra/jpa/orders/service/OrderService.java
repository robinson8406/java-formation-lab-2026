package com.indra.jpa.orders.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.indra.jpa.orders.model.Order;
import com.indra.jpa.orders.model.OrderStatus;
import com.indra.jpa.orders.repository.OrderRepository;

@Service
public class OrderService {

	private final OrderRepository orderRepository;

	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Transactional(readOnly = true)
	public List<OrderView> listByStatus(OrderStatus status) {
		return orderRepository.findByStatusWithItems(status).stream().map(OrderView::from).toList();
	}

	@Transactional(readOnly = true)
	public List<OrderView> listByStatusWithGraph(OrderStatus status) {
		return orderRepository.findWithGraphByStatus(status).stream().map(OrderView::from).toList();
	}

	@Transactional(readOnly = true)
	public Page<OrderView> listByStatus(OrderStatus status, Pageable pageable) {
		Page<Long> ids = orderRepository.findIdsByStatus(status, pageable);
		if (ids.isEmpty()) {
			return ids.map(id -> null);
		}
		Map<Long, Order> byId = orderRepository.findWithItemsByIdIn(ids.getContent()).stream()
				.collect(Collectors.toMap(Order::getId, Function.identity()));
		// El orden de la página lo define la lista de ids, no la query de fetch
		return ids.map(id -> OrderView.from(byId.get(id)));
	}
}