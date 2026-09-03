package com.indra.retail;

import com.indra.retail.strategy.DiscountStrategy;
import com.indra.retail.strategy.LoyaltyDiscountStrategy;
import com.indra.retail.strategy.SeasonalDiscountStrategy;
import com.indra.retail.strategy.StandardDiscountStrategy;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Calculador de descuentos para órdenes de compra.
 * Responsabilidad única (SRP): gestionar y aplicar las reglas de descuento.
 * Principio Open/Closed (OCP): desacoplado a través del patrón Strategy,
 * permitiendo registrar nuevas estrategias sin alterar la lógica interna.
 */
public class DiscountCalculator {

    private final Map<DiscountType, DiscountStrategy> strategies;

    /**
     * Constructor por defecto con las estrategias predeterminadas del sistema.
     */
    public DiscountCalculator() {
        this(List.of(
                new StandardDiscountStrategy(),
                new SeasonalDiscountStrategy(),
                new LoyaltyDiscountStrategy()
        ));
    }

    /**
     * Constructor con inyección de estrategias personalizadas para máxima extensibilidad (OCP).
     *
     * @param strategyList lista de estrategias a registrar
     */
    public DiscountCalculator(List<DiscountStrategy> strategyList) {
        this.strategies = new EnumMap<>(DiscountType.class);
        if (strategyList != null) {
            for (DiscountStrategy strategy : strategyList) {
                if (strategy != null && strategy.getDiscountType() != null) {
                    this.strategies.put(strategy.getDiscountType(), strategy);
                }
            }
        }
    }

    /**
     * Aplica el descuento correspondiente al tipo sobre el precio base.
     *
     * @param price precio base antes del descuento
     * @param type tipo de descuento a aplicar
     * @return precio final con el descuento aplicado, o el precio original si no aplica descuento
     */
    public BigDecimal apply(BigDecimal price, DiscountType type) {
        if (price == null) {
            throw new IllegalArgumentException("El precio no puede ser nulo");
        }
        if (type == null) {
            return price;
        }

        DiscountStrategy strategy = strategies.get(type);
        if (strategy == null) {
            return price;
        }

        return strategy.apply(price);
    }
}
