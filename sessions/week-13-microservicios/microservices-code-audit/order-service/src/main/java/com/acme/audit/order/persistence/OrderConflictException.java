package com.acme.audit.order.persistence;

public class OrderConflictException extends RuntimeException {
    private static final long serialVersionUID = -8259635753791976432L;

	public OrderConflictException(String message) { super(message); }
}
