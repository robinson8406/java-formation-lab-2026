package com.acme.audit.order.persistence;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.audit.order.application.port.out.OrderStore;
import com.acme.audit.order.enums.OrderStatusEnum;
import com.acme.audit.order.infrastructure.persistence.OrderEntity;
import com.acme.audit.order.infrastructure.persistence.OrderRepository;

@Service
public class OrderPersistenceService implements OrderStore {
	private final OrderRepository repository;

	public OrderPersistenceService(OrderRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public OrderData getOrCreate(String id, BigDecimal amount, String currency) {
        OrderEntity order = repository.findById(id).map(existing -> {
            if (existing.getAmount().compareTo(amount) != 0 || !existing.getCurrency().equals(currency)) {
                throw new OrderConflictException("Order identifier was already used with a different payload");
            }
            return existing;
        }).orElseGet(() -> repository.saveAndFlush(
                new OrderEntity(id, "ORDER-" + id + "-PAYMENT", amount, currency, OrderStatusEnum.PAYMENT_PENDING)));
        return toOrderData(order);
	}

	@Transactional
	public OrderData confirm(String id, String paymentId) {
		var order = repository.findById(id).orElseThrow();
        order.confirm(paymentId);
        return toOrderData(order);
	}

	@Transactional
    public OrderData markPaymentUnavailable(String id) {
		var order = repository.findById(id).orElseThrow();
        order.markPaymentUnavailable();
        return toOrderData(order);
    }

    private OrderData toOrderData(OrderEntity entity) {
        return new OrderData(entity.getOrderId(), entity.getIdempotencyKey(), entity.getAmount(),
                entity.getCurrency(), entity.getStatus().name(), entity.getPaymentId());
    }
}