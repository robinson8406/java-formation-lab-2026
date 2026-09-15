package com.indra.logistics.base.strategy;

import java.math.BigDecimal;

import com.indra.logistics.base.domain.PaymentMethod;

/** Contrato Strategy: cada método de pago sabe calcular su propia comisión y mensaje. */
public interface PaymentStrategy {

    PaymentMethod methodCode();

    BigDecimal calculateFee(BigDecimal amount);

    String confirmationMessage();

}

