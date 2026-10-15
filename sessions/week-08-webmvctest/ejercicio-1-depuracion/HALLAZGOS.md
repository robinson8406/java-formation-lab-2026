# Hallazgos — Ejercicio 1: catálogo de proveedores

Los cuatro fallos eran del código de producción. **Ningún test original estaba mal** y no se debilitó ninguna aserción.
Cada corrección va en un commit `fix:` separado. El refactor de la Parte B va en un commit `refactor:` posterior.

---

## Fallo 1 — El slice de `@WebMvcTest` no levanta el contexto
- **Síntoma**: los 5 tests de `SupplierControllerTest` fallan con
  `IllegalStateException: Failed to load ApplicationContext`, causado por
  `NoSuchBeanDefinitionException: No qualifying bean of type 'SupplierWebMapper' available`
  (parámetro 1 del constructor de `SupplierController`).
- **Causa raíz**: `@WebMvcTest` es un *slice*. Solo registra beans de la capa web (`@Controller`, `@ControllerAdvice`,
  `@JsonComponent`, `Converter`, `Filter`, `WebMvcConfigurer`, …). `SupplierWebMapper` era un `@Component` genérico,
  quedaba fuera del slice y el controller no se podía construir. En la aplicación completa sí funcionaba, por eso
  "en local corre" y el pipeline estaba en rojo.
- **Corrección**: el mapper es una función pura, sin estado ni dependencias, así que no hay motivo para que sea bean.
  Se quitó `@Component`, se convirtió en una clase `final` package-private con constructor privado y métodos `static`,
  y el controller dejó de recibirlo por constructor.
  Alternativas descartadas:
  - `@Import(SupplierWebMapper.class)` en el test: parchea el síntoma y habría que repetirlo en cada test que use el controller.
  - `@MockBean SupplierWebMapper`: obliga a simular una conversión trivial y el test deja de probar el mapeo real.
  - `@SpringBootTest`: prohibido por el enunciado y elimina el propósito del slice (rapidez y aislamiento).
- **Prevención**: regla de diseño "solo es bean lo que tiene dependencias o ciclo de vida". El slice test en el
  pipeline detecta de inmediato cualquier dependencia no web que se agregue al controller.

## Fallo 2 — Header `Location` mal formado en el POST
- **Síntoma**: `test3` falla con
  `Response header 'Location' expected:<http://localhost/api/suppliers/SUP-001> but was:<http://localhost/api/suppliersSUP-001>`.
- **Causa raíz**: `ServletUriComponentsBuilder.fromCurrentRequest().path("{id}")` concatena el segmento literal
  a la URI actual (`/api/suppliers`). `path()` no agrega el separador `/`.
- **Corrección**: `.path("/{id}")`. Se mantuvo `ServletUriComponentsBuilder` (y no un `URI.create("/api/suppliers/" + id)`)
  porque respeta el host, el puerto y el context path reales de la petición.
- **Prevención**: aserción exacta sobre el header `Location` en el test de creación. Es la que detectó el fallo y se conserva.

## Fallo 3 — El POST no valida el cuerpo de la petición
- **Síntoma**: `test4` falla con `Status expected:<400> but was:<500>`.
- **Causa raíz**: `CreateSupplierRequest` declara `@NotBlank`, `@Pattern` y `@Email`, pero sin `@Valid` en el parámetro
  Spring nunca ejecuta Bean Validation. La petición inválida llegaba al servicio. El mock, sin stub, devolvía `null`,
  `created.getId()` lanzaba `NullPointerException` y el handler genérico respondía 500. Además, ese 500 devolvía
  `exception.toString()` al cliente (ver refactor de Seguridad).
- **Corrección**: `create(@Valid @RequestBody CreateSupplierRequest request)`. Así se lanza `MethodArgumentNotValidException`
  y `ApiExceptionHandler` la traduce a `400 VALIDATION_ERROR` con la lista de campos inválidos.
- **Prevención**: cada endpoint con body tiene un test de entrada inválida que además verifica
  `verifyNoInteractions(supplierService)`. Eso prueba que la validación ocurre **antes** de llegar a la lógica de negocio.

## Fallo 4 — DELETE responde 200 en lugar de 204
- **Síntoma**: `test5` falla con `Status expected:<204> but was:<200>`.
- **Causa raíz**: `ResponseEntity.ok().build()` responde `200 OK` sin cuerpo. Para un borrado exitoso sin
  representación que devolver, el contrato REST del equipo y la semántica HTTP indican `204 No Content`.
- **Corrección**: `ResponseEntity.noContent().build()`.
- **Prevención**: el slice test fija el código de estado como parte del contrato y ahora además verifica
  `verify(supplierService).delete("SUP-001")`.

---

## Hallazgo adicional (pendiente) — Errores del cliente respondidos como 500

> **Estado: documentado, no implementado en este PR.** Se detectó en la prueba manual y queda propuesto
> para una iteración posterior, para no mezclarlo con los 4 fallos del alcance del ejercicio.

- **Síntoma**: al abrir `http://localhost:8081/api/suppliers` en el navegador (un `GET`) la API responde:
  ```json
  { "status": 500, "code": "INTERNAL_ERROR", "errors": ["ocurrió un error inesperado"], "path": "/api/suppliers" }

---

## Refactors aplicados

| Principio / práctica | Antes | Después | Test que lo respalda |
|---|---|---|---|
| **DIP (controller)** | `SupplierController` dependía de la clase concreta `SupplierServiceImpl` | Depende de la interfaz `SupplierService` (puerto en `application`) | `SupplierControllerTest` (mockea `SupplierService`) |
| **DIP (servicio)** | `SupplierServiceImpl` hacía `new InMemorySupplierRepository()`: imposible sustituir por JPA o por un doble de prueba, y el repo no era bean | Recibe `SupplierRepository` y `Clock` por constructor. `InMemorySupplierRepository` es `@Repository` | `SupplierServiceImplTest` (repo en memoria + `Clock.fixed`) |
| **SRP** | El controller normalizaba nombre y NIT y fijaba `internalNotes` con `LocalDateTime.now()` | El controller solo traduce HTTP ↔ `CreateSupplierCommand`. La normalización vive en el dominio y la nota de auditoría en el servicio, con `Clock` inyectado (determinista) | `SupplierTest.normalizesNameToUppercaseAndTaxIdWithoutDots`, `SupplierServiceImplTest.create_assignsSequentialIdNormalizesAndStampsInternalNote` |
| **Separación de capas** | El servicio recibía un `Supplier` armado por la capa web | El servicio recibe `CreateSupplierCommand` (record de `application`), sin depender de DTOs web | `create_delegatesRawDataToServiceAndReturns201WithLocation` |
| **Dominio / invariantes** | `Supplier` era un JavaBean mutable con setters públicos: podía existir sin nombre, sin id o con el NIT con puntos | `record Supplier` inmutable. El constructor compacto normaliza (`toUpperCase(Locale.ROOT)`, NIT sin puntos) y lanza `InvalidSupplierException` si se viola un invariante | `SupplierTest` (nombre en blanco, NIT inválido, id/email faltantes) |
| **Generación de identidad** | `AtomicLong` dentro del servicio + `setId()` sobre un objeto mutable | `SupplierRepository.nextId()`: el id existe antes de construir el `Supplier` | `InMemorySupplierRepositoryTest.nextId_generatesSequentialFormattedIds` |
| **Errores de dominio** | No existían: un dato inválido terminaba en NPE o se persistía | `InvalidSupplierException` → `400 VALIDATION_ERROR` | `create_whenDomainRejectsSupplier_returns400` |
| **Seguridad (fuga de información)** | El 500 devolvía `exception.toString()`: clase, mensaje y, potencialmente, URLs internas o credenciales | Mensaje genérico al cliente y detalle completo en el log (`log.error`) | `unexpectedError_returns500WithoutLeakingInternalDetails` |
| **Seguridad (exposición de datos)** | `internalNotes` no salía en la respuesta solo porque el mapper no lo incluía | Exclusión explícita y documentada en el mapper, verificada por test | `getById_returns200WithSupplierAndHidesInternalNotes` (`$.internalNotes` no existe) |
| **Entrada malformada** | Body vacío o JSON inválido → 500 | `HttpMessageNotReadableException` → `400 MALFORMED_REQUEST` | `create_withEmptyBody_returns400AndDoesNotCallService` |
| **Diseño web** | Mapper `@Component` inyectado en el controller: rompía el slice test | Utilidad estática package-private, sin bean | Todo `SupplierControllerTest` carga sin `@Import` |
| **DRY en el handler** | Construcción de `ErrorResponse` repetida y status duplicado como literal (`404`, `400`, `500`) | Método privado `error(status, code, errors, request)` que usa `status.value()` | Tests 400 / 404 / 500 del controller |
| **Tests legibles** | `test1` … `test5` | Patrón `metodo_escenario_resultado` (p. ej. `create_withBlankName_returns400AndDoesNotCallService`) | — |
| **Tests no frágiles** | `content().string("{…}")` dependía del orden y el formato exacto del JSON; `errors[0]` dependía del orden de los errores | `jsonPath` campo por campo; `hasItem(containsString("name"))` sobre la lista | `getById_returns200…`, `create_withBlankName_…` |
| **Verificación de interacciones** | No se verificaba qué recibía el servicio ni que no se le llamara con un body inválido | `verify(service).create(comandoEsperado)`, `verify(service).delete(id)` y `verifyNoInteractions` en los casos 400/405 | `create_delegatesRawData…`, `create_withBlankName_…`, `create_withEmptyBody_…`, `delete_existingSupplier_returns204` |


### Por qué cambió el test de creación del controller
Al mover la normalización al dominio, el controller ya no transforma datos. Con el servicio mockeado, el slice test
no puede ni debe comprobar que `"  acme ltda "` se convierte en `"ACME LTDA"`. Ahora el test verifica que el
controller entrega los datos **crudos** al servicio (`CreateSupplierCommand`) y serializa bien la respuesta.
La regla de normalización se prueba en `SupplierTest`, la capa donde vive.

### Observaciones adicionales (no aplicadas, propuestas para el equipo)
- La regex del NIT en el DTO (`[0-9.]{9,11}-[0-9]`) acepta valores como `"...........-1"`. La validación
  definitiva se hace en el dominio sobre el valor ya normalizado (`\d{9,10}-\d`).
- `@MockBean` está deprecado desde Spring Boot 3.4 a favor de `@MockitoBean`. Con la 3.2.5 actual sigue siendo válido,
  pero conviene migrarlo al actualizar.
- Spring 6 ofrece `ProblemDetail` (RFC 7807) como formato estándar de errores. Se mantuvo `ErrorResponse` para no
  romper el contrato existente; se puede evaluar como evolución de la API.

### Verificación
- `mvn -pl ejercicio-1-depuracion test` → todos los tests en verde.
- Prueba manual con la app levantada (`mvn -pl ejercicio-1-depuracion spring-boot:run`, puerto 8081):
  `POST /api/suppliers` → 201 con `Location`; `GET /api/suppliers/SUP-001` → 200; `DELETE` → 204;
  `GET /api/suppliers` → 405.