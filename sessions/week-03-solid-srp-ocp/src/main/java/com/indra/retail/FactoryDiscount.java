package com.indra.retail;

import java.util.Map;
import java.util.function.Supplier;

public class FactoryDiscount {

    private final Map<DiscountType, Supplier<Discounts>> discounts;

    public FactoryDiscount() {
        this.discounts = Map.of(
                DiscountType.STANDARD, StandardDiscount::new,
                DiscountType.SEASONAL, SeasonalDiscount::new,
                DiscountType.LOYALTY, LoyaltyDiscount::new
        );
    }

    public Discounts create(DiscountType type) {

        Supplier<Discounts> supplier = discounts.get(type);

        if (supplier == null) {
            throw new IllegalArgumentException(
                    "Tipo de descuento no soportado: " + type);
        }

        return supplier.get();
    }




}
