# Criterios de evaluación — Week 04

## Checklist del reviewer

### Strategy
- [ ] Interfaz `PaymentStrategy` con los 3 métodos requeridos
- [ ] 7 implementaciones concretas, cada una en su propio archivo
- [ ] `PaymentService` no referencia ninguna clase concreta de estrategia

### Factory
- [ ] `PaymentStrategyFactory.getStrategy()` funciona para los 7 metodos de pago
- [ ] `UnknownPaymentMethodException` lanzada para canal desconocido
- [ ] Agregar una nueva estrategia no requiere modificar `PaymentService`

### Tests
- [ ] Al menos 1 test por estrategia (independiente)
- [ ] Test para metodo desconocido en la Factory
- [ ] `mvn verify` en verde

### Calidad
- [ ] Sin `if-else` de metodo de pago en `PaymentService`

## Escala de madurez

| Junior | Semi-senior | Senior | Experto |
|--------|-------------|--------|---------|
| Strategy funcional, Factory con if-else | Factory limpia, tests por estrategia | Auto-descubrimiento con Spring @Component, OCP garantizado | Propone extensiones: decoradores, estrategias compuestas |
