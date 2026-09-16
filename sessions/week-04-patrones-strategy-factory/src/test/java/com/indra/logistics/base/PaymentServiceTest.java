package com.indra.logistics.base;

import com.indra.logistics.base.factory.PaymentStrategyFactory;
import com.indra.logistics.base.factory.PaymentStrategyFactoryImpl;
import com.indra.logistics.base.strategy.AmexPayment;
import com.indra.logistics.base.strategy.BankTransferPayment;
import com.indra.logistics.base.strategy.CashPayment;
import com.indra.logistics.base.strategy.DebitCardPayment;
import com.indra.logistics.base.strategy.MastercardPayment;
import com.indra.logistics.base.strategy.PaymentStrategy;
import com.indra.logistics.base.strategy.PaypalPayment;
import com.indra.logistics.base.strategy.VisaPayment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private final PaymentService service = new PaymentService(createFactory());

    private static PaymentStrategyFactory createFactory() {
        List<PaymentStrategy> strategies = List.of(
                new VisaPayment(),
                new MastercardPayment(),
                new PaypalPayment(),
                new CashPayment(),
                new BankTransferPayment(),
                new DebitCardPayment(),
                new AmexPayment()
        );
        return new PaymentStrategyFactoryImpl(strategies);
    }

    @ParameterizedTest
    @CsvSource({
            "VISA, 150.00, 5.25, 155.25, 'Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.'",
            "MASTERCARD, 80.00, 0.00, 80.00, 'Pago con tarjeta de crédito Mastercard procesado, monto no aplica comisión bancaria.'",
            "PAYPAL, 100.00, 2.00, 102.00, 'Pago con PayPal procesado, comisión de plataforma aplicada.'",
            "CASH, 100.00, 0.00, 100.00, 'Pago en efectivo registrado, sin comisión.'",
            "BANK_TRANSFER, 100.00, 2.50, 102.50, 'Pago por transferencia bancaria registrado, comisión bancaria aplicada.'",
            "DEBIT_CARD, 100.00, 4.00, 104.00, 'Pago con tarjeta de débito procesado, se aplica comisión bancaria.'",
            "AMEX, 80.00, 0.00, 80.00, 'Pago con tarjeta American Express procesado, monto no aplica comisión bancaria.'"
    })
    void processCalculatesPaymentResult(String method, String amount, String expectedFee,
                                        String expectedTotal, String expectedMessage) {
        BigDecimal paymentAmount = new BigDecimal(amount);

        PaymentResult result = service.process(new PaymentRequest(paymentAmount, method));

        assertEquals(method, result.method());
        assertEquals(paymentAmount, result.amount());
        assertEquals(0, new BigDecimal(expectedFee).compareTo(result.fee()));
        assertEquals(0, new BigDecimal(expectedTotal).compareTo(result.total()));
        assertEquals(expectedMessage, result.message());
    }
}