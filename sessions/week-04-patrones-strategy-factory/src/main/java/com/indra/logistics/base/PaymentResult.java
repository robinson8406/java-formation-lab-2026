package com.indra.logistics.base;

import java.math.BigDecimal;

public record PaymentResult(String method, BigDecimal amount, BigDecimal fee, BigDecimal total, String message) {
}
