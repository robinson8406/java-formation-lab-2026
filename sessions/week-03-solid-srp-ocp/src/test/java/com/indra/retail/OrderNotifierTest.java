package com.indra.retail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderNotifierTest {

    private final OrderNotifier orderNotifier = new OrderNotifier();

    @Test
    @DisplayName("Debe construir el mensaje de notificación con el email del cliente")
    void shouldBuildNotificationMessage() {
        String result = orderNotifier.notifyCustomer("cliente@indra.com", "Pedido procesado");

        assertEquals("[EMAIL a cliente@indra.com] Pedido procesado", result);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el email es nulo")
    void shouldThrowWhenEmailIsNull() {
        assertThrows(IllegalArgumentException.class, () -> orderNotifier.notifyCustomer(null, "mensaje"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el email está vacío")
    void shouldThrowWhenEmailIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> orderNotifier.notifyCustomer("  ", "mensaje"));
    }
}
