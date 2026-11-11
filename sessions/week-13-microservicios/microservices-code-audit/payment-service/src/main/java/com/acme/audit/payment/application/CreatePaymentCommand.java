package com.acme.audit.payment.application;

import java.math.BigDecimal;

public record CreatePaymentCommand(String orderId, BigDecimal amount, String currency) { }
