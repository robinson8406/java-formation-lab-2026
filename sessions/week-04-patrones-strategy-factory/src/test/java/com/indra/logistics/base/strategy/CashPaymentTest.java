package com.indra.logistics.base.strategy;

import com.indra.logistics.base.util.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CashPaymentTest {

	private final CashPayment payment = new CashPayment();

	@Test
	void methodCode() {
		assertEquals(PaymentMethod.CASH, payment.methodCode());
	}

	@Test
	void calculateFee() {
		assertEquals(new BigDecimal("0.00"), payment.calculateFee(new BigDecimal("150.00")));
	}

	@Test
	void confirmationMessage() {
		assertEquals("Pago en efectivo registrado, sin comisión.", payment.confirmationMessage());
	}

}