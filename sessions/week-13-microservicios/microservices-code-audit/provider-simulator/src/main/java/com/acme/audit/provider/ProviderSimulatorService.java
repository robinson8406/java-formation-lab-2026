package com.acme.audit.provider;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProviderSimulatorService {
    private final Map<String, Charge> charges = new ConcurrentHashMap<>();
    private final AtomicLong requests = new AtomicLong();
    private final long responseDelayMs;

    public ProviderSimulatorService(@Value("${provider.simulation.response-delay-ms:500}") long responseDelayMs) {
        this.responseDelayMs = responseDelayMs;
    }

    public ChargeResult charge(String key, String orderId, BigDecimal amount, String currency) {
        requests.incrementAndGet();
        Charge candidate = new Charge("PROV-" + UUID.randomUUID(), orderId, amount, currency);
        Charge existing = charges.putIfAbsent(key, candidate);
        Charge charge = existing == null ? candidate : existing;
        if (existing != null && !existing.matches(orderId, amount, currency)) {
            throw new IdempotencyKeyConflictException("Idempotency key was already used with a different payment payload");
        }
        return new ChargeResult(charge.providerTransactionId(), charge.orderId(), "APPROVED", existing != null);
    }

    public void delayResponse() {
        if (responseDelayMs <= 0) return;
        try { Thread.sleep(responseDelayMs); }
        catch (InterruptedException exception) { Thread.currentThread().interrupt(); }
    }

    public Metrics metrics() { return new Metrics(requests.get(), charges.size()); }

    public List<ChargeView> charges() {
        return charges.values().stream().map(c -> new ChargeView(c.providerTransactionId(), c.orderId(), c.amount(), c.currency())).toList();
    }

    public void reset() { charges.clear(); requests.set(0); }

    private record Charge(String providerTransactionId, String orderId, BigDecimal amount, String currency) {
        private boolean matches(String requestedOrderId, BigDecimal requestedAmount, String requestedCurrency) {
            return orderId.equals(requestedOrderId) && amount.compareTo(requestedAmount) == 0
                    && currency.equals(requestedCurrency);
        }
    }

    public static class IdempotencyKeyConflictException extends RuntimeException {
        private static final long serialVersionUID = 1L;

		public IdempotencyKeyConflictException(String message) { super(message); }
    }
    public record ChargeResult(String providerTransactionId, String orderId, String status, boolean replayed) { }
    public record ChargeView(String providerTransactionId, String orderId, BigDecimal amount, String currency) { }
    public record Metrics(long totalRequests, long uniqueCharges) { }
}
