package com.indra.logistics.base.strategy;

import com.indra.logistics.base.util.PaymentMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Contrato Strategy: cada método de pago sabe calcular su propia comisión y mensaje. */
public interface PaymentStrategy {

    PaymentMethod methodCode();

    BigDecimal calculateFee(BigDecimal amount);

    String confirmationMessage();

    default String confirmationMessage(BigDecimal amount) {
        return confirmationMessage();
    }




}
