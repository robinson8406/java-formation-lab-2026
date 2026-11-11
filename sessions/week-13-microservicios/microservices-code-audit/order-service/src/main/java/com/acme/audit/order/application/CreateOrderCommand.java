package com.acme.audit.order.application;

import java.math.BigDecimal;

public record CreateOrderCommand(String orderId, BigDecimal amount, String currency) { }
