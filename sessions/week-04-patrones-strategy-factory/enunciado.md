# Enunciado — Week 04: Patrones (Strategy/Factory)

## Qué viene dado en `week-04-start`

El start llega con:
- Interfaz `PaymentStrategy` ya definida con los 3 métodos (`calculateFee`, `methodCode`, `confirmationMessage`).
- Clase `CashPayment` ya implementada con la interfaz `PaymentStrategy`.
- `PaymentController` ya implementada y con su test en verde — sirve de modelo exacto.
- `PaymentService` con un `if-else` de medio de pago que debes eliminar y cambiarse por el patron.
- Clase `PaymentStrategyFactory` con firma definida pero sin implementación (método retorna `null`).

Tu trabajo es implementar el patron Strategy/Factory **estrategias + la factory** eliminando el `if-else` y que el PaymentControllerTest pase todo en verde.

---

## Contexto del reto

**Indra Logistics** necesita cambiar la forma en que se obtienen las comiciones según los metodos de pago a un modelo que no genere regresiones ya que actualmente todo esta dentro de un bloque `if-else`.

## Lo que debes implementar

**Tarea 1 — implementar estrategias**

Siguiendo los valores retornados por el bloque `if-else` se requieren crear las respectivas estrategias para cada uno de los metodos de pago ejemplo :
- `CASH`: fee = `0`; total = amount + fee; message `Pago en efectivo registrado, sin comisión.`.
- `VISA`: fee = `amount * 0.035` (roundUp); total = `amount + fee`; message = `Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.`.
- 1 test por cada estrategia (copiar la estructura del test de `PaymentControllerTest`).

**Tarea 2 — Completar `PaymentStrategyFactory`**
- Puede ser con un switch o el uso de un listado automatico @Component
```java
public PaymentStrategyFactory getStrategy(String methodCode) {
    return switch (methodCode) {
        case "CASH"         -> new CashPayment();
        //TODO
        default             -> throw new UnknownPaymentMethodException(methodCode);
    };
}

```

Elimina el `if-else` de canal en `PaymentService` usando la factory.

## Restricciones técnicas (para todos)

- Agregar nuevas estrategias no debe requerir modificar `PaymentService`.
- **Criterio no funcional (calidad)**: cada estrategia en su propio archivo; los tests de `PaymentControllerTest` no deben verse afectados.

## Criterio de aceptación del PR

- [ ] Cada estrategia implementada con su respectivo test implementadas con su test
- [ ] `PaymentStrategyFactory` completa (7 medios de pago + excepción para desconocido)
- [ ] `PaymentService` sin `if-else` de metodo de pago
- [ ] Tests del start siguen en verde
- [ ] `mvn verify` en verde

## Bonus (opcional)

- Registrar las estrategias como `@Component` de Spring y que la factory las descubra automáticamente desde `List<ShippingStrategy>`.
