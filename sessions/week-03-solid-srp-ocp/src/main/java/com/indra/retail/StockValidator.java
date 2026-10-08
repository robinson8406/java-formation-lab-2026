package com.indra.retail;

public class StockValidator {

    public boolean hasEnoughStock(int availableStock, int requestedQuantity) {
        return availableStock >= requestedQuantity;
    }
}
