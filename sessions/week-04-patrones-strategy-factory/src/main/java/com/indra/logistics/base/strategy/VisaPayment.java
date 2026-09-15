package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.indra.logistics.base.domain.PaymentMethod;
import com.indra.logistics.base.util.PaymentUtils;

/**
 * VisaPayment
 */
public class VisaPayment implements PaymentStrategy {
    private String message;

    @Override
    public PaymentMethod methodCode() {
        return PaymentMethod.VISA;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {

        BigDecimal fee = BigDecimal.ZERO;
        message = "Pago con tarjeta de crédito Visa procesado, monto no aplica comisión bancaria.";
        // Si el monto es mayor o igual a 100, no se aplica comisión
        if (amount.compareTo(BigDecimal.valueOf(100)) >= 0) {
            BigDecimal tariff = PaymentUtils.creditCardTariff().add(BigDecimal.valueOf(0.005));
            fee = amount.multiply(tariff).setScale(2, RoundingMode.HALF_UP);
            message = "Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.";
        }

        return fee;

    }

    @Override
    public String confirmationMessage() {
        return message;
    }

}
