package com.acme.audit.order.usecases;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import com.acme.audit.order.application.CreateOrderCommand;
import com.acme.audit.order.application.port.out.OrderStore;
import com.acme.audit.order.application.port.out.OrderStore.OrderData;
import com.acme.audit.order.application.port.out.PaymentGateway;

@Service
public class OrderApplicationServiceUseCase {
    private final OrderStore orderStore;
    private final PaymentGateway paymentGateway;

    public OrderApplicationServiceUseCase(OrderStore orderStore, PaymentGateway paymentGateway) {
        this.orderStore = orderStore;
        this.paymentGateway = paymentGateway;
    }

    public OrderView create(CreateOrderCommand command) {
        OrderData order = orderStore.getOrCreate(command.orderId(), command.amount(), command.currency());
        if (order.paymentId() != null) {
            return OrderView.from(order);
        }

        try {
            var payment = paymentGateway.charge(order.idempotencyKey(), order.orderId(), order.amount(), order.currency());
            return OrderView.from(orderStore.confirm(command.orderId(), payment.paymentId()));
        } catch (RestClientException failure) {
            orderStore.markPaymentUnavailable(command.orderId());
            throw failure;
        }
    }

    public record OrderView(String orderId, String status, String paymentId) {
        private static OrderView from(OrderData order) {
            return new OrderView(order.orderId(), order.status(), order.paymentId());
        }
    }
}
