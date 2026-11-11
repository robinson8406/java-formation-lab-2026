package com.indra.jpa.orders.web;

import com.indra.jpa.orders.model.OrderStatus;
import com.indra.jpa.orders.service.OrderService;
import com.indra.jpa.orders.service.OrderView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping
	public List<OrderView> list(@RequestParam(defaultValue = "CONFIRMED") OrderStatus status) {
		return orderService.listByStatus(status);
	}

	@GetMapping("/paged")
	public Page<OrderView> listPaged(@RequestParam(defaultValue = "CONFIRMED") OrderStatus status,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return orderService.listByStatus(status, PageRequest.of(page, size));
	}
}