package com.acme.audit.order.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;

import com.acme.audit.order.enums.OrderStatusEnum;

@Entity
@Table(name = "orders")
public class OrderEntity {
	@Id
	@Column(length = 80)
	private String orderId;
	@Column(nullable = false, unique = true, length = 150)
	private String idempotencyKey;
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;
	@Column(nullable = false, length = 3)
	private String currency;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private OrderStatusEnum status;
	@Column(length = 36)
	private String paymentId;

	protected OrderEntity() {
	}

	public OrderEntity(String id, String key, BigDecimal amount, String currency, OrderStatusEnum status) {
		this.orderId = id;
		this.idempotencyKey = key;
		this.amount = amount;
		this.currency = currency;
		this.status = status;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public OrderStatusEnum getStatus() {
		return status;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void confirm(String id) {
		paymentId = id;
		status = OrderStatusEnum.CONFIRMED;
	}

	public void markPaymentUnavailable() {
		status = OrderStatusEnum.PAYMENT_UNAVAILABLE;
	}
}