package com.indra.logistics.base;

import java.math.BigDecimal;

public record PaymentRequest(BigDecimal amount, String method) {
}
