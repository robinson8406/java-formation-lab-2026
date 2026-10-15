# Hallazgos — Ejercicio 1 (catálogo de proveedores)

Los fallos aparecían en cascada: el fallo 1 impedía arrancar el contexto del slice y ocultaba los otros 3.
Cada corrección está en su propio commit `fix: ...`.

## Fallo 1 — El contexto del slice no arranca

- **Síntoma**: los 5 tests fallan con `Failed to load ApplicationContext` →
  `NoSuchBeanDefinitionException: No qualifying bean of type 'SupplierWebMapper'`.
- **Causa raíz**: `@WebMvcTest` no levanta toda la aplicación. Solo registra beans de la capa web
  (`@Controller`, `@ControllerAdvice`, `Filter`, `WebMvcConfigurer`, `Converter`, `@JsonComponent`…).
  `SupplierWebMapper` era un `@Component` normal, así que el escaneo del slice lo excluye y el controller
  no se puede construir.
- **Corrección**: el mapper deja de ser un bean. El mapeo es una función pura sin estado, así que no necesita
  Spring: primero se convirtió en métodos estáticos y en el refactor pasó a `SupplierResponse.from(...)` y
  `CreateSupplierRequest.toCommand()`.
  Alternativa válida pero peor: `@Import(SupplierWebMapper.class)` en el test. Funciona, pero cada colaborador
  nuevo del controller obligaría a tocar el test.
- **Prevención**: el controller solo debe depender de abstracciones que el test mockea con `@MockBean`. Un
  colaborador que no sea web ni mock es una señal de que la dependencia sobra o está en la capa equivocada.

## Fallo 2 — `Location` mal formado

- **Síntoma**: `Response header 'Location' expected:<http://localhost/api/suppliers/SUP-001> but was:<http://localhost/api/suppliersSUP-001>`.
- **Causa raíz**: `ServletUriComponentsBuilder.fromCurrentRequest()` parte de `/api/suppliers` y `.path("{id}")`
  concatena el segmento sin separador.
- **Corrección**: `.path("/{id}")`.
- **Prevención**: verificar siempre el valor completo de `Location` en el test de creación (no solo que exista).

## Fallo 3 — Request inválido devuelve 500 en vez de 400

- **Síntoma**: `Status expected:<400> but was:<500>`.
- **Causa raíz**: faltaba `@Valid` en el `@RequestBody`. Sin él, las anotaciones `@NotBlank`, `@Email` y
  `@Pattern` del DTO nunca se evalúan. El request inválido llegaba al servicio (un mock que devolvía `null`),
  el controller hacía `null.getName()` y el `NullPointerException` terminaba en el handler genérico → `500`.
  El handler genérico **enmascaró** un error de validación como si fuera un fallo del servidor.
- **Corrección**: `@Valid @RequestBody CreateSupplierRequest request`.
- **Prevención**: un test de `400` por cada endpoint que recibe body, con `verifyNoInteractions(service)` para
  demostrar que la petición inválida no llega al caso de uso.

## Fallo 4 — `DELETE` devuelve 200

- **Síntoma**: `Status expected:<204> but was:<200>`.
- **Causa raíz**: `ResponseEntity.ok().build()`. El contrato dice `204 No Content` porque no hay body que devolver.
- **Corrección**: `ResponseEntity.noContent().build()`.
- **Prevención**: tests que verifiquen el código exacto del contrato y `verify(service).delete(id)`, porque sin
  body no hay otra forma de saber que el controller llamó al caso de uso.

## Refactors aplicados

| Principio / práctica | Antes | Después | Test que lo respalda |
|---|---|---|---|
| DIP (controller) | Dependía de `SupplierServiceImpl` (clase concreta) | Depende de la interfaz `SupplierService`; implementación `DefaultSupplierService` | `SupplierControllerTest` con `@MockBean SupplierService` |
| DIP (servicio) | `new InMemorySupplierRepository()` dentro del servicio | `SupplierRepository` y `SupplierIdGenerator` inyectados por constructor; adaptadores con `@Repository` / `@Component` | `DefaultSupplierServiceTest` (sin Spring, con mocks) |
| SRP | Normalización (mayúsculas, NIT sin puntos) y notas internas en el controller | Normalización en el constructor de `Supplier`; notas internas en el servicio | `SupplierTest`, `DefaultSupplierServiceTest` |
| Dominio que protege sus invariantes | Clase mutable con setters; podía quedar sin nombre o sin NIT | `record Supplier` inmutable que valida y normaliza al construirse | `SupplierTest` |
| Testabilidad del tiempo | `LocalDateTime.now()` en el controller: no determinista | `Clock` inyectado (`ClockConfiguration`); en el test, `Clock.fixed(...)` | `DefaultSupplierServiceTest#createAssigns...` |
| Seguridad | El handler genérico devolvía `exception.toString()` (clase y mensaje interno) | Mensaje genérico al cliente y `log.error` con la excepción completa | `SupplierControllerTest#returns500WithoutInternalDetails...` |
| Aplicación sin DTO web | El controller construía la entidad desde el DTO | `CreateSupplierRequest.toCommand()` → `CreateSupplierCommand` | `SupplierControllerTest#returns201...` (verifica el command) |
| Contrato público | — | `SupplierResponse` no expone `internalNotes` | `SupplierControllerTest#returns200...` (`internalNotes` no existe) |
| Tests | `test1..test5`, body comparado como String, sin `verify`, sin `Content-Type` | Nombres de comportamiento, `@Nested` por endpoint, `jsonPath`, `content().contentType(...)`, `verify` / `verifyNoInteractions` | Toda la suite |
| Organización | `ErrorResponse` mezclado con los DTO de negocio | Paquete `web.error` con `ApiExceptionHandler` y `ErrorResponse` | — |

### Un test del controller cambió, y es correcto

El antiguo `test3` afirmaba que el controller devolvía `"ACME LTDA"` a partir de `"  acme ltda "`, es decir,
probaba la normalización a través del controller. Esa regla ya no vive en `web`: ahora el test del controller
verifica que entrega los datos **tal cual** al caso de uso (`verify(...).create(new CreateSupplierCommand("  acme ltda ", ...))`)
y la normalización se prueba donde vive, en `SupplierTest`. No es una aserción debilitada: es la misma regla,
probada en la capa correcta.

## Mediciones

| Comando | Tests | Tiempo reportado por Surefire |
|---|---|---|
| `mvn -pl ejercicio-1-depuracion test` | 19 | `SupplierControllerTest` ≈ 2,3 s (casi todo es el arranque del contexto, una vez por clase) |
