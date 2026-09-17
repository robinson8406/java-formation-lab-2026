package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class AmexPayment implements PaymentStrategy {

    private static final String METHOD_CODE = "AMEX";
    private static final BigDecimal FEE_THRESHOLD = BigDecimal.valueOf(100);
    private static final BigDecimal COMMISSION_RATE = BigDecimal.valueOf(0.03);

    private final ThreadLocal<String> messageHolder = ThreadLocal.withInitial(
            () -> "Pago con tarjeta American Express procesado, se aplica comisión bancaria.");

    @Override
    public String methodCode() {
        return METHOD_CODE;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount.compareTo(FEE_THRESHOLD) < 0) {
            messageHolder.set("Pago con tarjeta American Express procesado, monto no aplica comisión bancaria.");
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        messageHolder.set("Pago con tarjeta American Express procesado, se aplica comisión bancaria.");
        return amount.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage() {
        return messageHolder.get();
    }
}
