package com.indra.retail;

import com.indra.retail.strategy.DiscountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("DiscountCalculator — Tests unitarios de cálculo de descuentos (SRP y OCP)")
class DiscountCalculatorTest {

    private DiscountCalculator discountCalculator;

    @BeforeEach
    void setUp() {
        discountCalculator = new DiscountCalculator();
    }

    @Nested
    @DisplayName("Descuento estándar (STANDARD - 5%)")
    class StandardDiscountTests {

        @Test
        @DisplayName("Debe aplicar 5% de descuento para tipo STANDARD")
        void shouldApplyStandardDiscount() {
            BigDecimal result = discountCalculator.apply(new BigDecimal("100.00"), DiscountType.STANDARD);
            assertEquals(0, new BigDecimal("95.00").compareTo(result));
        }
    }

    @Nested
    @DisplayName("Descuento de temporada (SEASONAL - 20%)")
    class SeasonalDiscountTests {

        @Test
        @DisplayName("Debe aplicar 20% de descuento para tipo SEASONAL")
        void shouldApplySeasonalDiscount() {
            BigDecimal result = discountCalculator.apply(new BigDecimal("100.00"), DiscountType.SEASONAL);
            assertEquals(0, new BigDecimal("80.00").compareTo(result));
        }
    }

    @Nested
    @DisplayName("Descuento de fidelidad (LOYALTY - 15%)")
    class LoyaltyDiscountTests {

        @Test
        @DisplayName("Debe aplicar 15% de descuento para clientes fidelizados (LOYALTY)")
        void shouldApplyLoyaltyDiscount() {
            BigDecimal result = discountCalculator.apply(new BigDecimal("100.00"), DiscountType.LOYALTY);
            assertEquals(0, new BigDecimal("85.00").compareTo(result));
        }
    }

    @Nested
    @DisplayName("Pruebas parametrizadas de cálculo")
    class ParameterizedDiscountTests {

        @ParameterizedTest(name = "Precio base: {0}, Tipo: {1} => Esperado: {2}")
        @CsvSource({
                "100.00, STANDARD, 95.00",
                "200.00, STANDARD, 190.00",
                "100.00, SEASONAL, 80.00",
                "50.00,  SEASONAL, 40.00",
                "100.00, LOYALTY,  85.00",
                "200.00, LOYALTY,  170.00",
                "0.00,   LOYALTY,  0.00"
        })
        @DisplayName("Debe calcular correctamente el precio final según el tipo y monto")
        void shouldCalculateCorrectlyForVariousInputs(String basePrice, DiscountType type, String expectedPrice) {
            BigDecimal result = discountCalculator.apply(new BigDecimal(basePrice), type);
            assertEquals(0, new BigDecimal(expectedPrice).compareTo(result));
        }
    }

    @Nested
    @DisplayName("Casos de borde y validaciones")
    class EdgeCasesAndValidationTests {

        @Test
        @DisplayName("Debe retornar el precio original si el tipo de descuento es nulo")
        void shouldReturnOriginalPriceWhenTypeIsNull() {
            BigDecimal price = new BigDecimal("150.00");
            BigDecimal result = discountCalculator.apply(price, null);
            assertEquals(price, result);
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el precio es nulo")
        void shouldThrowExceptionWhenPriceIsNull() {
            assertThrows(IllegalArgumentException.class, () -> discountCalculator.apply(null, DiscountType.STANDARD));
        }
    }

    @Nested
    @DisplayName("Verificación de Extensibilidad (Principio Open/Closed - OCP)")
    class OpenClosedPrincipleTests {

        @Test
        @DisplayName("Debe permitir registrar una nueva estrategia personalizada sin modificar DiscountCalculator")
        void shouldAllowCustomStrategyWithoutModifyingCalculator() {
            // Demuestra que la arquitectura es abierta para extensión (OCP)
            DiscountStrategy customVipStrategy = new DiscountStrategy() {
                @Override
                public DiscountType getDiscountType() {
                    return DiscountType.LOYALTY; // simula override de estrategia
                }

                @Override
                public BigDecimal apply(BigDecimal price) {
                    return price.multiply(BigDecimal.valueOf(0.50)); // 50% VIP
                }
            };

            DiscountCalculator customCalculator = new DiscountCalculator(List.of(customVipStrategy));
            BigDecimal result = customCalculator.apply(new BigDecimal("100.00"), DiscountType.LOYALTY);

            assertEquals(0, new BigDecimal("50.00").compareTo(result));
        }
    }
}
