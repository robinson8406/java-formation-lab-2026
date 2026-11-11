# Enunciado — Week 13: Microservicios y resiliencia distribuida

## Qué viene dado en `microservices-code-audit`

El proyecto contiene tres servicios:

- `order-service`: recibe pedidos y solicita los pagos.
- `payment-service`: procesa los pagos y se comunica con el proveedor.
- `provider-simulator`: simula un proveedor externo con respuestas lentas.

La comunicación sigue este flujo:

`order-service` → `payment-service` → `provider-simulator`

El proyecto utiliza Java 21, Spring Boot y Resilience4j. Incluye pruebas de aceptación, algunas de ellas en rojo.

Configuración inicial:

| Servicio | Retry | Wait duration |
|---|---:|---:|
| `order-service` | 3 intentos | 100 ms |
| `payment-service` | 3 intentos | 100 ms |

El proveedor `provider-simulator` tiene configurado un retraso de respuesta de **500 ms**.

## Contexto del reto

La plataforma está generando múltiples solicitudes para una misma operación de pago. Esto está sobrecargando al proveedor y puede provocar pagos duplicados.

Además, algunos servicios reciben un timeout aunque el proveedor posiblemente ya haya procesado el pago.

Tu trabajo es identificar la causa y corregir el comportamiento de los microservicios.


## Cómo ejecutar el reto

Para iniciar el flujo de procesamiento de pedidos, realiza una petición HTTP `POST` al siguiente endpoint:

**URL:** `http://localhost:8081/orders`

**Método:** `POST`

**Body (JSON):**

{
  "orderId": "ORD-1001",
  "amount": 250000.00,
  "currency": "COP"
}

## Lo que debes implementar

**Tarea 1 — Corregir la estrategia de Retry**

Analiza los reintentos de ambos servicios y define una estrategia que evite solicitudes redundantes.

El o los servicios responsable debe controlar los reintentos hacia el proveedor, sin multiplicar innecesariamente las operaciones de pago.

**Tarea 2 — Garantizar la idempotencia**

Corrige el manejo de `Idempotency-Key` para garantizar que una misma operación conserve su clave durante todos los reintentos y no genere pagos duplicados.

**Tarea 3 — Validar el comportamiento**

Ejecuta las pruebas existentes y verifica que los timeouts y reintentos no provoquen solicitudes ni pagos duplicados.

## Restricciones técnicas

- Los cambios solo están permitidos en `order-service` y `payment-service`.
- **No modificar `provider-simulator`**, sus tiempos de respuesta ni su comportamiento.
- No modificar, eliminar ni deshabilitar las pruebas de aceptación.
- Conservar la comunicación HTTP entre los servicios con `RestClient`.
- No generar nuevas **claves** de idempotencia para una misma operación.

## Criterio de aceptación del PR

- Identificar y corregir la causa de la multiplicación de solicitudes.
- Definir una estrategia de Retry controlada y coherente.
- Conservar la misma `Idempotency-Key` durante los reintentos.
- Evitar pagos duplicados.
- Todas las pruebas de aceptación en verde, sin modificaciones.
- `mvn verify` en verde.

## Bonus (opcional)
- Analizar el uso de Circuit Breaker y el manejo de resultados inciertos.
