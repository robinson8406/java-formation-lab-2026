package com.indra.logistics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.indra.logistics.base.factory.PaymentStrategyFactoryImpl;

import com.indra.logistics.base.strategy.PaymentStrategy;


@SpringBootTest(classes = BaseApplication.class)
class PaymentControllerTest {

    @Autowired
    private PaymentStrategyFactoryImpl factory;
    private final PaymentController paymentController = new PaymentController(factory);

    @Test
    void registeredMethodCodes_AreDiscoveredFromSpringContext() {
        assertEquals(
                List.of("AMEX", "BANK_TRANSFER", "CASH", "DEBIT_CARD", "MASTERCARD", "PAYPAL", "VISA"),
                factory.registeredMethodCodes());


    }

    @Test
    void getFee_PaymentMethod_Cash_Success() {

        PaymentStrategy strategy = factory.getStrategy("CASH");
        BigDecimal amount = new BigDecimal("100.00");
    
        assertEquals("CASH", strategy.methodCode());
        assertEquals(new BigDecimal("0.00"), strategy.calculateFee(amount));
        assertEquals(new BigDecimal("100.00"), amount.add(strategy.calculateFee(amount)));
        assertEquals("Pago en efectivo registrado, sin comisión.", strategy.confirmationMessage(amount));
    }

    @Test
    void getFee_PaymentMethod_BankTransfer_Success() {

        PaymentStrategy strategy = factory.getStrategy("BANK_TRANSFER");
        BigDecimal amount = new BigDecimal("300.00");
    
        assertEquals("BANK_TRANSFER", strategy.methodCode());
        assertEquals(new BigDecimal("7.50"), strategy.calculateFee(amount));
        assertEquals(new BigDecimal("307.50"), amount.add(strategy.calculateFee(amount)));
        assertEquals("Pago por transferencia bancaria registrado, comisión bancaria aplicada.", strategy.confirmationMessage(amount));
    }

    @Test
    void getFee_PaymentMethod_DebitCard_Success() {

        PaymentStrategy strategy = factory.getStrategy("DEBIT_CARD");
        BigDecimal amount = new BigDecimal("100.00");
    
        assertEquals("DEBIT_CARD", strategy.methodCode());
        assertEquals(new BigDecimal("4.00"), strategy.calculateFee(amount));
        assertEquals(new BigDecimal("104.00"), amount.add(strategy.calculateFee(amount)));
        assertEquals("Pago con tarjeta de débito procesado, se aplica comisión bancaria.", strategy.confirmationMessage(amount));
    }

    @Test
    void getFee_PaymentMethod_MasterCard_Success() {

        PaymentStrategy strategy = factory.getStrategy("MASTERCARD");
        BigDecimal amount = new BigDecimal("100.00");
    
        assertEquals("MASTERCARD", strategy.methodCode());
        assertEquals(new BigDecimal("3.00"), strategy.calculateFee(amount));
        assertEquals(new BigDecimal("103.00"), amount.add(strategy.calculateFee(amount)));
        assertEquals("Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.", strategy.confirmationMessage(amount));
    }

    @Test
    void getFee_PaymentMethod_Visa_Success() {

        PaymentStrategy strategy = factory.getStrategy("VISA");
        BigDecimal amount = new BigDecimal("200.00");
    
        assertEquals("VISA", strategy.methodCode());
        assertEquals(new BigDecimal("7.00"), strategy.calculateFee(amount));
        assertEquals(new BigDecimal("207.00"), amount.add(strategy.calculateFee(amount)));
        assertEquals("Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.", strategy.confirmationMessage(amount));
    }

    @Test
    void getFee_PaymentMethod_AmericanExpress_Success() {

        PaymentStrategy strategy = factory.getStrategy("AMEX");
        BigDecimal amount = new BigDecimal("200.00");
    
        assertEquals("AMEX", strategy.methodCode());
        assertEquals(new BigDecimal("6.00"), strategy.calculateFee(amount));
        assertEquals(new BigDecimal("206.00"), amount.add(strategy.calculateFee(amount)));
        assertEquals("Pago con tarjeta de crédito American Express procesado, se aplica comisión bancaria.", strategy.confirmationMessage(amount));
    }

    @Test
    void getFee_PaymentMethod_PayPal_Success() {

        PaymentStrategy strategy = factory.getStrategy("PAYPAL");
        BigDecimal amount = new BigDecimal("100.00");
    
        assertEquals("PAYPAL", strategy.methodCode());
        assertEquals(new BigDecimal("2.00"), strategy.calculateFee(amount));
        assertEquals(new BigDecimal("102.00"), amount.add(strategy.calculateFee(amount)));
        assertEquals("Pago con PayPal procesado, comisión de plataforma aplicada.", strategy.confirmationMessage(amount));
    }

    @Test
    void getFee_InvalidPaymentMethod_BadRequest() {
        UnknownPaymentMethodException exception = null;
        try {
            factory.getStrategy("CRYPTO");
        } catch (UnknownPaymentMethodException ex) {
            exception = ex;
        }
        ResponseEntity<Map<String, String>> response = paymentController.handleUnknownMethod(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("error", "método de pago 'CRYPTO' no soportado"), response.getBody());
    }

}
