# Enunciado — Week 01: Kickoff + Setup Java 21

## Contexto del reto

El equipo de desarrollo de **Indra Logistics** acaba de migrar su microservicio de seguimiento de envíos a Java 21. Tu tarea es incorporarte al proyecto, verificar que el entorno esté correctamente configurado y agregar la primera funcionalidad usando el flujo estándar del equipo: rama personal → implementación → PR.

## Lo que debes implementar

1. Clona el repo y comprueba que `mvn verify` pasa en verde en la rama `week-01-start`.
2. Crea tu rama personal: `week-01/<tu-alias>`.
3. Implementa la clase `TrackingIdGenerator` con el método:
   ```java
   public String generate(String origin, String destination)
   ```
   Debe retornar un ID con el formato `ORIG-DEST-XXXXXXXX` (8 caracteres alfanuméricos aleatorios, en mayúsculas).
4. Escribe al menos 2 tests JUnit 5 que validen el formato del ID generado.

## Restricciones técnicas (para todos)

- Usar **Java 21** (verificar con `java -version`).
- El método no debe recibir ni retornar `null`; lanzar `IllegalArgumentException` si algún parámetro es nulo o vacío.
- Mínimo 2 tests: uno para el formato y uno para la validación de parámetros inválidos.
- **Criterio no funcional (calidad)**: el código debe ser legible sin comentarios — nombres de variables y métodos autodescriptivos.

## Criterio de aceptación del PR

- [ ] `mvn verify` pasa en verde
- [ ] El formato del ID generado es correcto en todos los tests
- [ ] Se validan correctamente los parámetros de entrada
- [ ] Sin lógica duplicada
- [ ] Commits con Conventional Commits

## Bonus (opcional)

- Usar un **Record** de Java 21 para encapsular los parámetros `origin` y `destination`.
- Agregar un test parametrizado con `@ParameterizedTest` para múltiples combinaciones.
