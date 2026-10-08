# Enunciado — Week 02: TDD con JUnit 5

## Contexto del reto

El equipo de **Indra Transport** necesita un Programador de Rutas para su modulo de transporte. El Product Owner entregó los criterios de aceptación, pero **no existe ningún código todavía**. Tu trabajo es implementar la lógica usando TDD estricto: escribe el test primero, hazlo fallar, luego escribe el mínimo código para que pase.

## Lo que debes implementar

Usando el ciclo **red → green → refactor**:

1. `ProgramadorRutas.debeValidarTipoRutasYBuses()` los buses electricos solo pueden ir a rutas electricas, los demas a todas las rutas:
2. `ProgramadorRutas.consultarHorariosPorTipoBus(bus,tipo)` debe retornar los horarios por tipo de bus:
   - bus desconocido: lanzar `IllegalArgumentException`
   - tipo desconocido: lanzar `UnsupportedTypeException`
3. `ProgramadorRutas.debeRechazarHorarioSolapado()` Un bus no puede tener dos horarios que se solapen. 
   - Ejemplo si ya tiene 08:00->10:00 no puedo programar 08:30->10:30
4. `ProgramadorRutas.debeRechazarHorarioRangoInvalido()` Un bus no puede tener una hora de llegada menosr a la hora de salida.      
   - Ejemplo no se permite 10:00->08:00
5. Parametros del horario null o vacio: crear test y feat para contemplar estos casos.

## Restricciones técnicas (para todos)

- **El test debe escribirse antes que la implementación** (commits separados recomendados: `test: ...` luego `feat: ...`).
- Usar anotaciones JUnit 5: `@Test`, `@DisplayName`, `@ParameterizedTest`, `@ValueSource` o `@CsvSource`.
- Sin lógica condicional en los tests (no `if` dentro de un test).
- **Criterio no funcional (calidad)**: los tests deben ser el primer lugar donde se entiende qué hace el sistema — deben leerse como especificaciones.

## Criterio de aceptación del PR

- [ ] Tests escritos antes del código (se aprecia en el historial de commits)
- [ ] Al menos 5 tests cubriendo los escenarios descritos
- [ ] `mvn verify` en verde
- [ ] Sin lógica condicional dentro de los métodos de test
- [ ] Nombres de test descriptivos con `@DisplayName`

## Bonus (opcional)

- Implementar el objeto Tipo (Bus y Ruta)
- Implementar `@ParameterizedTest` con `@CsvSource` para los escenarios de horario solapado.
- Agregar un test de integración que use `@Nested` para CuandoUnBusYaTieneHorariosProgramados.
