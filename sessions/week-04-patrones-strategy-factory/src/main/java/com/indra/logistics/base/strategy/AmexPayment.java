package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.indra.logistics.base.domain.PaymentMethod;
import com.indra.logistics.base.util.PaymentUtils;

@Component
public class AmexPayment implements PaymentStrategy {

    private String message;

    @Override
    public PaymentMethod methodCode() {
        return PaymentMethod.AMEX;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        BigDecimal fee = BigDecimal.ZERO;
        message = "Pago con tarjeta American Express procesado, monto no aplica comisión bancaria.";

        if (amount.compareTo(BigDecimal.valueOf(100)) >= 0) {

            BigDecimal tariff = PaymentUtils.creditCardTariff().add(BigDecimal.ZERO);
            fee = amount.multiply(tariff).setScale(2, RoundingMode.HALF_UP);
            message = "Pago con tarjeta American Express procesado, se aplica comisión bancaria.";
        } else {

        }

        return fee;
    }

    @Override
    public String confirmationMessage() {
        return message;
    }
}
