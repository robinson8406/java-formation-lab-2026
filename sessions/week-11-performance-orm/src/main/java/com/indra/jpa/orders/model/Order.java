package com.indra.jpa.orders.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// @Getter/@Setter en lugar de @Data: toString/equals recorren relaciones LAZY y ocultan N+1
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String customerName;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	private LocalDateTime createdAt = LocalDateTime.now();

	// LAZY a propósito: iterar items dispara 1 query por orden (N+1)
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	public Order(String customerName, OrderStatus status) {
		this.customerName = customerName;
		this.status = status;
	}

	public void addItem(OrderItem item) {
		item.setOrder(this);
		items.add(item);
	}
}