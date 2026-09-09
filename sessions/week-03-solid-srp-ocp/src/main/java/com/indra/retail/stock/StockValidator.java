package com.indra.retail.stock;

public class StockValidator {

    public boolean hasEnoughStock(int availableStock, int requestedQuantity) {
        return availableStock >= requestedQuantity;
    }
}
