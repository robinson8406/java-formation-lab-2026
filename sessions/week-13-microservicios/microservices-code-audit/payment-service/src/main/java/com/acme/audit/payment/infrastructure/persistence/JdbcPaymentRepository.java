package com.acme.audit.payment.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import com.acme.audit.payment.application.port.out.PaymentRepository;
import com.acme.audit.payment.domain.PaymentResult;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactionTemplate;

    public JdbcPaymentRepository(JdbcTemplate jdbc, TransactionTemplate transactionTemplate) {
        this.jdbc = jdbc;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Optional<StoredPayment> findByIdempotencyKey(String key) {
        return jdbc.query("SELECT payment_id, order_id, amount, currency, payment_status FROM payments WHERE idempotency_key=?",
                (rs, row) -> new StoredPayment(
                        new PaymentResult(rs.getString("payment_id"), rs.getString("order_id"),
                                rs.getString("payment_status"), true),
                        rs.getString("order_id"), rs.getBigDecimal("amount"), rs.getString("currency")), key)
                .stream().findFirst();
    }

    @Override
    public PaymentResult save(String key, String orderId, BigDecimal amount, String currency, String status) {
        return transactionTemplate.execute(transaction -> {
            String paymentId = UUID.randomUUID().toString();
            jdbc.update("INSERT INTO payments(payment_id,idempotency_key,order_id,amount,currency,payment_status) VALUES(?,?,?,?,?,?)",
                    paymentId, key, orderId, amount, currency, status);
            return new PaymentResult(paymentId, orderId, status, false);
        });
    }

    @Override
    public long count() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM payments", Long.class);
        return count == null ? 0 : count;
    }
}
