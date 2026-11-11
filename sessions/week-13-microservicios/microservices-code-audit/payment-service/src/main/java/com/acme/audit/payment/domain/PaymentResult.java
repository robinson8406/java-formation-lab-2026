package com.acme.audit.payment.domain;

public record PaymentResult(String paymentId, String orderId, String status, boolean replayed) {
}