package com.acme.audit.payment.usecases;

import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.acme.audit.payment.application.CreatePaymentCommand;
import com.acme.audit.payment.application.port.out.PaymentProviderPort;
import com.acme.audit.payment.application.port.out.PaymentRepository;
import com.acme.audit.payment.application.port.out.PaymentRepository.StoredPayment;
import com.acme.audit.payment.domain.PaymentResult;

@Service
public class PaymentApplicationServiceUseCase {
    private final PaymentRepository paymentRepository;
    private final PaymentProviderPort paymentProvider;

    public PaymentApplicationServiceUseCase(PaymentRepository paymentRepository, PaymentProviderPort paymentProvider) {
        this.paymentRepository = paymentRepository;
        this.paymentProvider = paymentProvider;
    }

    public PaymentResult process(String idempotencyKey, CreatePaymentCommand command) {
        Optional<StoredPayment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return replayOrReject(existing.get(), command);
        }

        // Never hold a database transaction open while waiting for a remote provider.
        PaymentProviderPort.ProviderCharge charge = paymentProvider.charge(idempotencyKey, command.orderId(),
                command.amount(), command.currency());
        try {
            return paymentRepository.save(idempotencyKey, command.orderId(), command.amount(),
                    command.currency(), charge.status());
        } catch (DuplicateKeyException concurrentRequest) {
            // Another request won the unique-key race. The provider receives the same idempotency key.
            return paymentRepository.findByIdempotencyKey(idempotencyKey)
                    .map(stored -> replayOrReject(stored, command))
                    .orElseThrow(() -> concurrentRequest);
        }
    	
    }

    public long countPayments() {
        return paymentRepository.count();
    }

    private PaymentResult replayOrReject(StoredPayment stored, CreatePaymentCommand command) {
        boolean samePayload = stored.orderId().equals(command.orderId())
                && stored.amount().compareTo(command.amount()) == 0
                && stored.currency().equals(command.currency());
        if (!samePayload) {
            throw new IdempotencyKeyConflictException(
                    "Idempotency key was already used with a different payment payload");
        }
        return stored.result();
    }

    public static class IdempotencyKeyConflictException extends RuntimeException {
        private static final long serialVersionUID = 1L;

		public IdempotencyKeyConflictException(String message) { super(message); }
    }
}
