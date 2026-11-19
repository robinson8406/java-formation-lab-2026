package com.acme.audit.order.persistence;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
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
        Optional<OrderEntity> existing = repository.findById(id);
        if (existing.isPresent()) {
            return toOrderData(validateSamePayload(existing.get(), amount, currency));
        }

        try {
            OrderEntity created = repository.saveAndFlush(
                    new OrderEntity(id, "ORDER-" + id + "-PAYMENT", amount, currency, OrderStatusEnum.PAYMENT_PENDING));
            return toOrderData(created);
        } catch (DataIntegrityViolationException concurrentInsert) {
            OrderEntity winner = repository.findById(id).orElseThrow(() -> concurrentInsert);
            return toOrderData(validateSamePayload(winner, amount, currency));
        }
    }

    private OrderEntity validateSamePayload(OrderEntity existing, BigDecimal amount, String currency) {
        if (existing.getAmount().compareTo(amount) != 0 || !existing.getCurrency().equals(currency)) {
            throw new OrderConflictException("Order identifier was already used with a different payload");
        }
        return existing;
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

    @Transactional(readOnly = true)
    public OrderData findById(String id) {
        var order = repository.findById(id).orElseThrow();
        return toOrderData(order);
    }
}