# Criterios de evaluación — Week 02

## Checklist del reviewer

### TDD
- [ ] Existen commits de test anteriores a commits de implementación
- [ ] Los tests fallan antes de que exista la implementación (se deduce del historial)

### Tests
- [ ] Al menos 8 tests cubriendo los escenarios del enunciado
- [ ] Tests para casos borde: Bus null o vacio, Tipo de bus desconocido, horarios solapados
- [ ] `@DisplayName` o nombres de método que explican el escenario
- [ ] Sin `if` ni lógica condicional dentro de los tests

### Funcionalidad
- [ ] Cálculo de horario correcto para tipo de Bus y Ruta
- [ ] Asignacion correcta de horarios sin solapamiento de horas
- [ ] `UnsupportedTypeException` lanzada para tipos desconocidos
- [ ] `IllegalArgumentException` para rutas, buses u horarios desconocidos

### Calidad
- [ ] `mvn verify` en verde
- [ ] Sin código de producción sin tests que lo respalde

## Escala de madurez

| Junior | Semi-senior | Senior | Experto |
|--------|-------------|--------|---------|
| Tests post-implementación, casos básicos | Tests primero, cubre casos borde | Tests parametrizados, @Nested, diseño guiado por tests | Identifica ambigüedades en los requisitos y los convierte en tests adicionales |
