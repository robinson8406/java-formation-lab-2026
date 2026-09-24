# Criterios de evaluación — Week 01

## Checklist del reviewer

### Funcionalidad
- [ ] `TrackingIdGenerator.generate()` retorna IDs con el formato `ORIG-DEST-XXXXXXXX`
- [ ] Se lanza `IllegalArgumentException` para parámetros nulos o vacíos
- [ ] Los 8 caracteres aleatorios son alfanuméricos y en mayúsculas

### Tests
- [ ] Al menos 2 tests presentes y ejecutándose
- [ ] Los tests verifican comportamiento, no la implementación interna
- [ ] Los tests tienen nombres descriptivos (`@DisplayName` o nombre de método claro)

### Calidad de código
- [ ] Sin código muerto ni comentarios redundantes
- [ ] Nombres de variables y métodos autodescriptivos
- [ ] Sin números mágicos sin contexto

### Flujo de trabajo
- [ ] PR apunta a `week-01-start`
- [ ] Al menos 1 commit con formato Conventional Commits
- [ ] Pipeline verde (`mvn verify`)

## Escala de madurez

| Junior | Semi-senior | Senior | Experto |
|--------|-------------|--------|---------|
| Implementa y tiene tests básicos que pasan | Aplica validaciones de input, tests con casos borde | Usa Record de Java 21, tests parametrizados, código limpio | Propone mejoras al diseño inicial, identifica edge cases no listados |
