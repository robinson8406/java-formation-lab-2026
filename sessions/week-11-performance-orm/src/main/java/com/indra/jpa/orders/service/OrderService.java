package com.indra.jpa.orders.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	@Transactional(readOnly = true)
	public List<OrderView> listByStatus(OrderStatus status) {
		return orderRepository.findByStatusWithItems(status).stream().map(OrderView::from).toList();
	}
	
	@Transactional(readOnly = true)
	public List<OrderView> listByStatusOtherWay(OrderStatus status) {
		return orderRepository.findOrdersByStatus(status).stream().map(OrderView::from).toList();
	}

	@Transactional(readOnly = true)
	public Page<OrderView> listByStatus(OrderStatus status, Pageable pageable) {
		return orderRepository.findByStatus(status, pageable).map(OrderView::from);
	}
}