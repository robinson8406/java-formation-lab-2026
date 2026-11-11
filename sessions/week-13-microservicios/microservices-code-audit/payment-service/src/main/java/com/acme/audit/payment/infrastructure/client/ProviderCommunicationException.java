package com.acme.audit.payment.infrastructure.client;

public class ProviderCommunicationException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7335394406273639794L;

	public ProviderCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}