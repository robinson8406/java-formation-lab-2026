package com.indra.jpa.orders.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.indra.jpa.orders.model.OrderStatus;
import com.indra.jpa.orders.repository.OrderRepository;

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
		Pageable stablePageable = pageable.getSort().isUnsorted()
				? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").ascending())
				: pageable;
		Page<Long> orderIds = orderRepository.findIdsByStatus(status, stablePageable);
		if (orderIds.isEmpty()) {
			return new PageImpl<>(List.of(), stablePageable, orderIds.getTotalElements());
		}

		Map<Long, OrderView> ordersById = orderRepository.findAllWithItemsByIdIn(orderIds.getContent()).stream()
				.map(OrderView::from)
				.collect(Collectors.toMap(OrderView::id, Function.identity()));

		List<OrderView> content = orderIds.getContent().stream()
				.map(ordersById::get)
				.toList();
		return new PageImpl<>(content, stablePageable, orderIds.getTotalElements());
	}
}