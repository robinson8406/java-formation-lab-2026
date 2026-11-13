# Criterios de evaluación — Week 10

## Checklist del reviewer

### Transaccionalidad
- [ ] `@Transactional` solo en la capa de servicio
- [ ] `rollbackFor = InsufficientFundsException.class` presente (checked exception)
- [ ] `TransferAuditService` usa `propagation = REQUIRES_NEW`
- [ ] Sin self-invocation transaccional

### Tests
- [ ] Test 1: saldo insuficiente → rollback verificado, ambas cuentas sin cambio
- [ ] Test 2: transferencia fallida → log de auditoría persistido aunque la transferencia haga rollback
- [ ] Ambos tests comprueban el comportamiento real, sin `assertTrue(true)` ni otras aserciones provisionales
- [ ] `mvn verify` en verde

### Calidad
- [ ] Sin `@Transactional` en controller o repository
- [ ] La atomicidad es evidente en el código (una sola unidad de trabajo)

## Bonus (opcional, no condiciona la aprobación)

- [ ] Tercer test: transferencia exitosa con saldos correctos verificados
- [ ] Explicación en el PR de por qué usar `REQUIRES_NEW` en lugar de `NESTED` para este caso

## Escala de madurez

Todos los niveles deben cumplir el checklist obligatorio. La madurez se evidencia en la calidad de la solución y su explicación; los bonus son opcionales.

| Junior | Semi-senior | Senior | Experto |
|--------|-------------|--------|---------|
| Implementa las dos tareas y verifica los dos escenarios obligatorios | Explica el rollback de la checked exception y la transacción independiente | Tests claros que demuestran el rollback y la persistencia de la auditoría | Justifica los límites transaccionales y sus implicaciones operativas en este caso |
