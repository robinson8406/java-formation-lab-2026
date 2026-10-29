# Enunciado — Week 10: Transacciones

## Qué viene dado en `week-10-start`

El start llega con:
- Entidades `Account` con campos `id`, `owner`, `balance`.
- `TransferService` con el método `transfer()` esquelético: descuenta y acredita, pero **sin `@Transactional`**. Si algo falla, el saldo puede quedar inconsistente.
- `TransferAuditService` con el método `log()` sin implementar.
- 2 tests con los nombres de los escenarios obligatorios y un `assertTrue(true)` provisional, junto con la preparación de cuentas y utilidades de comprobación.

El esqueleto pasa `mvn verify`, pero todavía no resuelve el reto. Tu trabajo es sustituir las aserciones provisionales por comprobaciones reales e implementar el comportamiento transaccional necesario para que ambos tests pasen.

---

## Contexto del reto

**Indra Bank** ha tenido incidentes en producción donde el débito se aplica pero el crédito falla, dejando el sistema inconsistente. Además, el log de auditoría se pierde cuando la transferencia falla porque está en la misma transacción.

## Lo que debes implementar

**Tarea 1 — Hacer `transfer()` atómico**

Configura la transacción de `transfer()` para que el débito y el crédito sean atómicos, incluso cuando se lance `InsufficientFundsException`.

`InsufficientFundsException` es una **checked exception**: ten en cuenta las reglas de rollback por defecto de Spring.

**Tarea 2 — Log de auditoría independiente**

Implementa `TransferAuditService.log()` para persistir el evento y configura una transacción independiente que permita guardar el log aunque la transferencia haga rollback.

**Los 2 tests obligatorios deben validar lo siguiente:**
- Test 1: saldo insuficiente → ambas cuentas sin cambio (rollback verificado).
- Test 2: transferencia fallida → log de auditoría guardado de todas formas.

## Restricciones técnicas (para todos)

- `@Transactional` solo en la capa de servicio — nunca en controller ni repository.
- Sin llamadas `@Transactional` entre métodos de la misma clase (self-invocation).
- **Criterio no funcional (operación)**: el log de auditoría debe sobrevivir al rollback de la transferencia — demostrado por el Test 2.

## Criterio de aceptación del PR

- [ ] Test 1 (rollback por saldo insuficiente) en verde
- [ ] Test 2 (log survives rollback) en verde
- [ ] Ambos tests comprueban el comportamiento real, sin aserciones provisionales
- [ ] `rollbackFor = InsufficientFundsException.class` presente
- [ ] `TransferAuditService` con `REQUIRES_NEW`
- [ ] `mvn verify` en verde

## Bonus (opcional)

- Agregar un 3.er test: transferencia exitosa con saldos correctos verificados.
- Explicar en el PR por qué `REQUIRES_NEW` en lugar de `NESTED` para este caso.
