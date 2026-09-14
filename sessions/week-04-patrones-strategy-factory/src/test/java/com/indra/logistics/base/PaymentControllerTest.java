package com.indra.logistics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class PaymentControllerTest {

    private final PaymentController paymentController = new PaymentController();

    @Nested
    @DisplayName("Verificar comisiones y totales metodo de pago VISA")
    class GetFeeTests {

        @Nested
        class VisaTests {
            @Test
            @DisplayName("Verificar comisiones y totales metodo de pago VISA valores mayores a 100")
            void getFee_VisaAmountAbove100_Success() {
                PaymentResult result = paymentController.getFee("VISA", new BigDecimal("150.00"));

                assertEquals("VISA", result.method());
                assertEquals(new BigDecimal("5.25"), result.fee());
                assertEquals(new BigDecimal("155.25"), result.total());
                assertEquals("Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.", result.message());
            }

            @Test
            @DisplayName("Verificar comisiones y totales metodo de pago VISA valores menores a 100")
            void getFee_Visa_Success() {
                PaymentResult result = paymentController.getFee("VISA", new BigDecimal("80.50"));

                assertEquals("VISA", result.method());
                assertEquals(BigDecimal.ZERO, result.fee());
                assertEquals(new BigDecimal("80.50"), result.total());
                assertEquals("Pago con tarjeta de crédito Visa procesado, monto no aplica comisión bancaria.", result.message());
            }



        }

        @Nested
        @DisplayName("Verificar comisiones y totales metodo de pago MASTERCARD")
        class MastercardTests {
            @Test
            @DisplayName("Verificar comisiones y totales metodo de pago MASTERCARD valores mayores a 100")
            void getFee_MastercardAmountAbove100_Success() {
                PaymentResult result = paymentController.getFee("MASTERCARD", new BigDecimal("150.00"));

                assertEquals("MASTERCARD", result.method());
                assertEquals(new BigDecimal("4.50"), result.fee());
                assertEquals(new BigDecimal("154.50"), result.total());
                assertEquals("Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.", result.message());
            }



            @Test
            @DisplayName("Verificar comisiones y totales metodo de pago MASTERCARD valores menores a 100")
            void getFee_Mastercard_Success() {
                PaymentResult result = paymentController.getFee("MASTERCARD", new BigDecimal("80.00"));

                assertEquals("MASTERCARD", result.method());
                assertEquals(BigDecimal.ZERO, result.fee());
                assertEquals(new BigDecimal("80.00"), result.total());
                assertEquals("Pago con tarjeta de crédito Mastercard procesado, monto no aplica comisión bancaria.", result.message());
            }

        }

        @Nested
        @DisplayName("Verificar comisiones y totales metodo de pago AMEX")
        class AmexTests {
            @Test
            @DisplayName("Verificar comisiones y totales metodo de pago AMEX valores mayores a 100")
            void getFee_AmexAmountAbove100_Success() {
                PaymentResult result = paymentController.getFee("AMEX", new BigDecimal("150.00"));

                assertEquals("AMEX", result.method());
                assertEquals(new BigDecimal("4.50"), result.fee());
                assertEquals(new BigDecimal("154.50"), result.total());
                assertEquals("Pago con tarjeta American Express procesado, se aplica comisión bancaria.", result.message());
            }


            @Test
            @DisplayName("Verificar comisiones y totales metodo de pago AMEX valores menores a 100")
            void getFee_Amex_Success() {
                PaymentResult result = paymentController.getFee("AMEX", new BigDecimal("80.00"));

                assertEquals("AMEX", result.method());
                assertEquals(BigDecimal.ZERO, result.fee());
                assertEquals(new BigDecimal("80.00"), result.total());
                assertEquals("Pago con tarjeta American Express procesado, monto no aplica comisión bancaria.", result.message());
            }


        }

    }


    @Nested
    @DisplayName("Verificar comisiones y totales otros metodos de pago")
    class otherPaymentMethodsTests {

        @Test
        void getFee_PaymentMethodWithoutAmount_Success() {
            PaymentResult result = paymentController.getFee("CASH");

            assertEquals("CASH", result.method());
            assertEquals(new BigDecimal("100"), result.amount());
            assertEquals(new BigDecimal("0.00"), result.fee());
            assertEquals(new BigDecimal("100.00"), result.total());
            assertEquals("Pago en efectivo registrado, sin comisión.", result.message());
        }


        @Test
        void getFee_PayPal_Success() {
            PaymentResult result = paymentController.getFee("PAYPAL", new BigDecimal("100.00"));

            assertEquals("PAYPAL", result.method());
            assertEquals(new BigDecimal("2.00"), result.fee());
            assertEquals(new BigDecimal("102.00"), result.total());
            assertEquals("Pago con PayPal procesado, comisión de plataforma aplicada.", result.message());
        }

        @Test
        void getFee_BankTransfer_Success() {
            PaymentResult result = paymentController.getFee("BANK_TRANSFER", new BigDecimal("100.00"));

            assertEquals("BANK_TRANSFER", result.method());
            assertEquals(new BigDecimal("2.50"), result.fee());
            assertEquals(new BigDecimal("102.50"), result.total());
            assertEquals("Pago por transferencia bancaria registrado, comisión bancaria aplicada.", result.message());
        }
        @Test
        void getFee_DebitCard_Success() {
            PaymentResult result = paymentController.getFee("DEBIT_CARD", new BigDecimal("100.00"));

            assertEquals("DEBIT_CARD", result.method());
            assertEquals(new BigDecimal("4.00"), result.fee());
            assertEquals(new BigDecimal("104.00"), result.total());
            assertEquals("Pago con tarjeta de débito procesado, se aplica comisión bancaria.", result.message());
        }


    }


    @Test
    @DisplayName("Verificar manejo de excepciones para metodo de pago desconocido")
    void getFee_InvalidPaymentMethod_BadRequest() {
        UnknownPaymentMethodException exception = null;
        try {
            paymentController.getFee("CRYPTO", new BigDecimal("100.00"));
        } catch (UnknownPaymentMethodException ex) {
            exception = ex;
        }
        ResponseEntity<Map<String, String>> response = paymentController.handleUnknownMethod(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("error", "método de pago 'CRYPTO' no soportado"), response.getBody());
    }




}
