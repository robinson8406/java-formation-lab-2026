package com.indra.jpa.orders.service;

import com.indra.jpa.orders.model.Order;
import com.indra.jpa.orders.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public record OrderView(Long id, String customerName, String status, List<ItemView> items) {

	public record ItemView(String productName, int quantity, BigDecimal unitPrice) {
	}

	public static OrderView from(Order order) {
		return new OrderView(order.getId(), order.getCustomerName(), order.getStatus().name(),
				order.getItems().stream().map(OrderView::item).toList());
	}

	private static ItemView item(OrderItem item) {
		return new ItemView(item.getProduct().getName(), item.getQuantity(), item.getProduct().getPrice());
	}
}