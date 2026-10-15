# Hallazgos — Ejercicio 1

## Fallo 1 — El slice web no encuentra el mapper
- **Síntoma**: `SupplierControllerTest` no puede iniciar el `ApplicationContext` porque no encuentra `SupplierWebMapper`.
- **Causa raíz**: `@WebMvcTest(SupplierController.class)` carga componentes web, no componentes genéricos marcados con `@Component`; el mapper era un bean fuera del slice.
- **Corrección**: se convirtió el mapper en una clase sin estado con métodos estáticos. El controller no necesita que el contenedor administre una transformación determinista.
- **Prevención**: mantener dependencias de slice explícitas y ejecutar los tests de controller con `@WebMvcTest`.

## Fallo 2 — El header `Location` concatena mal el identificador
- **Síntoma**: el test de creación recibe `/api/suppliersSUP-001` en vez de `/api/suppliers/SUP-001`.
- **Causa raíz**: `ServletUriComponentsBuilder.path("{id}")` agrega el texto sin un separador entre la ruta actual y el identificador.
- **Corrección**: se agregó el segmento `/{id}`.
- **Prevención**: validar el header `Location` completo en el test de creación, no solo comprobar que exista.

## Fallo 3 — El POST acepta un request inválido
- **Síntoma**: el caso con `name` vacío no produce el `400 VALIDATION_ERROR` esperado.
- **Causa raíz**: las restricciones Bean Validation del DTO solo se evalúan si el parámetro `@RequestBody` tiene `@Valid`.
- **Corrección**: se agregó `@Valid` al parámetro del controller sin modificar las restricciones ni las aserciones.
- **Prevención**: cada DTO de entrada debe activar validación en el límite HTTP y tener un test de request inválido.

## Fallo 4 — DELETE incumple el contrato HTTP
- **Síntoma**: el controller respondía `200 OK` donde el contrato exige `204 No Content`.
- **Causa raíz**: se construía una respuesta OK explícita aunque la operación no devuelve representación.
- **Corrección**: el endpoint devuelve `ResponseEntity.noContent()`.
- **Prevención**: mantener status, headers y cuerpo como parte de los criterios de aceptación del endpoint.

## Fallo adicional — Acoplamiento impide sustituir dependencias
- **Síntoma**: no era uno de los cuatro fallos de ejecución de `SupplierControllerTest`; el controller requería `SupplierServiceImpl` y el servicio construía su propio repositorio, incumpliendo DIP y complicando sustituir dependencias en pruebas.
- **Causa raíz**: las dependencias apuntaban a detalles de implementación en vez de abstracciones, violando DIP y dejando al contenedor sin control del adaptador de persistencia.
- **Corrección**: se introdujo `SupplierService`, se inyectó por constructor en el controller y el servicio recibe `SupplierRepository`; el adaptador en memoria es un bean Spring.
- **Prevención**: verificar dependencias por interfaces con tests de slice y cableado, y mantener la construcción de dependencias fuera de las clases de aplicación.

## Refactors aplicados

| Principio / práctica | Antes | Después | Test que lo respalda |
|---|---|---|---|
| DIP | Controller dependía de `SupplierServiceImpl`; servicio instanciaba un repositorio | Controller depende de `SupplierService`; servicio recibe `SupplierRepository` por constructor | `SupplierControllerTest` y ejecución de `mvn verify` |
| SRP | Controller normalizaba nombre, NIT y generaba notas | Controller solo traduce HTTP; `Supplier` normaliza y el servicio añade metadatos | `SupplierTest` |
| Invariantes de dominio | Entidad mutable sin validación | `Supplier` es un record inmutable que normaliza y rechaza campos vacíos | `SupplierTest` |
| Mapper en slice web | Mapper era un bean que `@WebMvcTest` no carga automáticamente | Mapper determinista sin dependencia de Spring | `SupplierWebMapperTest` |
| Manejo seguro de errores | El body exponía `exception.toString()` | Se registra el error en servidor y se responde con mensaje genérico | `unexpectedErrorsDoNotExposeInternalDetails` |
| Robustez de tests | Nombres genéricos y comparación del JSON como cadena | Nombres por comportamiento y aserciones `jsonPath` | `SupplierControllerTest` |
