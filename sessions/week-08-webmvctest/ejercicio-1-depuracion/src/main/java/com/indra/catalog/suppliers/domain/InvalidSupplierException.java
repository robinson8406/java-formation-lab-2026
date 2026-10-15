package com.indra.catalog.suppliers.domain;

public class InvalidSupplierException extends RuntimeException {
    public InvalidSupplierException(String message) {
        super(message);
    }
}
