package com.indra.logistics.base.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.indra.logistics.base.UnknownPaymentMethodException;
import com.indra.logistics.base.strategy.CashPayment;
import com.indra.logistics.base.strategy.PaymentStrategy;

class PaymentStrategyFactoryTest {

    private final PaymentStrategyFactory factory = new PaymentStrategyFactoryImpl();

    @Nested
    @DisplayName("Resolución de medios de pago soportados")
    class MetodosSoportados {

        @ParameterizedTest(name = "Medio {0} debe resolverse correctamente")
        @ValueSource(strings = {
            "CASH",
            "VISA",
            "MASTERCARD",
            "AMEX",
            "PAYPAL",
            "BANK_TRANSFER",
            "DEBIT_CARD"
        })
        @DisplayName("Factory resuelve los 7 medios de pago soportados")
        void getStrategy_SupportedMethods_ReturnsStrategy(String methodCode) {
            PaymentStrategy strategy = factory.getStrategy(methodCode);
            assertNotNull(strategy);
            assertEquals(methodCode, strategy.methodCode());
        }
    }

    @Nested
    @DisplayName("Manejo de errores y medios no soportados")
    class MetodosNoSoportados {

        @Test
        @DisplayName("Método desconocido lanza UnknownPaymentMethodException con mensaje descriptivo")
        void getStrategy_UnknownMethod_ThrowsException() {
            UnknownPaymentMethodException ex = assertThrows(
                UnknownPaymentMethodException.class,
                () -> factory.getStrategy("BITCOIN")
            );
            assertEquals("método de pago 'BITCOIN' no soportado", ex.getMessage());
        }

        @Test
        @DisplayName("Método nulo lanza UnknownPaymentMethodException")
        void getStrategy_NullMethod_ThrowsException() {
            assertThrows(
                UnknownPaymentMethodException.class,
                () -> factory.getStrategy(null)
            );
        }
    }

    @Nested
    @DisplayName("Soporte de auto-descubrimiento / OCP")
    class AutoDescubrimiento {

        @Test
        @DisplayName("Factory soporta inyección por lista (Spring auto-discovery)")
        void constructorWithList_SupportsDynamicDiscovery() {
            PaymentStrategy customStrategy = new CashPayment();
            PaymentStrategyFactory dynamicFactory = new PaymentStrategyFactoryImpl(List.of(customStrategy));

            PaymentStrategy resolved = dynamicFactory.getStrategy("CASH");
            assertNotNull(resolved);
            assertEquals("CASH", resolved.methodCode());
        }
    }
}
