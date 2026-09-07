package com.indra.retail.validation;

public class StockValidator {

    public boolean hasEnoughStock(int availableStock, int requestedQuantity) {
        return availableStock >= requestedQuantity;
    }
}
