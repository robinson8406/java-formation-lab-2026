package com.acme.audit.order.infrastructure.client;

public class PaymentCommunicationException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PaymentCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}