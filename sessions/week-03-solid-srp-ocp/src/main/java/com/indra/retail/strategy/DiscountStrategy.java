package com.indra.retail.strategy;

import com.indra.retail.DiscountType;
import java.math.BigDecimal;

/**
 * Estrategia de descuento para el cálculo de precios.
 * Permite cumplir con el principio Open/Closed (OCP), habilitando
 * la adición de nuevas reglas de descuento sin modificar clases existentes.
 */
public interface DiscountStrategy {

    /**
     * Tipo de descuento que gestiona esta estrategia.
     */
    DiscountType getDiscountType();

    /**
     * Aplica el descuento sobre el precio base proporcionado.
     *
     * @param price precio base antes del descuento
     * @return precio con el descuento aplicado
     */
    BigDecimal apply(BigDecimal price);
}
