package com.indra.logistics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService();

    @Nested
    @DisplayName("Procesamiento de pagos válidos")
    class PagosValidos {

        @Test
        @DisplayName("Procesar CASH sin comisión")
        void process_Cash_Success() {
            PaymentRequest request = new PaymentRequest(new BigDecimal("150.00"), "CASH");
            PaymentResult result = paymentService.process(request);

            assertNotNull(result);
            assertEquals("CASH", result.method());
            assertEquals(new BigDecimal("150.00"), result.amount());
            assertEquals(new BigDecimal("0.00"), result.fee());
            assertEquals(new BigDecimal("150.00"), result.total());
            assertEquals("Pago en efectivo registrado, sin comisión.", result.message());
        }

        @ParameterizedTest(name = "Medio {0}, Monto {1} -> Fee {2}, Total {3}")
        @CsvSource({
            "VISA, 200.00, 7.00, 207.00",
            "VISA, 50.00, 0.00, 50.00",
            "MASTERCARD, 200.00, 6.00, 206.00",
            "AMEX, 100.00, 3.00, 103.00",
            "PAYPAL, 100.00, 2.00, 102.00",
            "BANK_TRANSFER, 100.00, 2.50, 102.50",
            "DEBIT_CARD, 100.00, 4.00, 104.00"
        })
        @DisplayName("Cálculo correcto de total = amount + fee para cada estrategia")
        void process_CalculatesFeeAndTotalCorrectly(String method, String amount, String expectedFee, String expectedTotal) {
            PaymentRequest request = new PaymentRequest(new BigDecimal(amount), method);
            PaymentResult result = paymentService.process(request);

            assertEquals(method, result.method());
            assertEquals(new BigDecimal(expectedFee), result.fee());
            assertEquals(new BigDecimal(expectedTotal), result.total());
        }
    }

    @Nested
    @DisplayName("Manejo de errores")
    class ManejoErrores {

        @Test
        @DisplayName("Lanza UnknownPaymentMethodException si el método es desconocido")
        void process_UnknownMethod_ThrowsException() {
            PaymentRequest request = new PaymentRequest(new BigDecimal("100.00"), "WESTERN_UNION");
            assertThrows(UnknownPaymentMethodException.class, () -> paymentService.process(request));
        }
    }
}
