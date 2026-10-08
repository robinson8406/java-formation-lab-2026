# HALLAZGOS — Ejercicio 1: depuración y refactor de `SupplierController`

## Fallo 1 — El mapper como `@Component` bloquea todo el slice
- **Síntoma**: los 5 tests de `SupplierControllerTest` fallan con `IllegalStateException: Failed to load
  ApplicationContext`, causado por `UnsatisfiedDependencyException: No qualifying bean of type
  'SupplierWebMapper'`.
- **Causa raíz**: `@WebMvcTest(SupplierController.class)` registra únicamente los beans de la capa web que
  Spring Boot reconoce como tal (`@Controller`, `@RestControllerAdvice`, convertidores, `WebMvcConfigurer`,
  filtros, etc.) más lo que se declare explícitamente con `@Import`. `SupplierWebMapper` estaba anotado
  `@Component`, una estereotipia genérica que el slice **no** escanea. El `SupplierController` dependía de
  él por constructor, así que Spring no podía instanciar el controller y el contexto completo fallaba
  (y con él, los 5 tests).
- **Corrección**: `SupplierWebMapper` dejó de ser un bean de Spring. Es una clase sin estado y sin
  dependencias (solo traduce DTO ↔ dominio), así que se convirtió en una utilidad con métodos estáticos
  (`toDomain`, `toResponse`) invocada directamente por el controller. No hay motivo de diseño para que un
  traductor puro viva en el contenedor de IoC.
- **Prevención**: cuando una clase de la capa `web` no tiene estado ni dependencias inyectables, preferir
  una utilidad estática sobre un bean. Si en el futuro necesita ser un bean (p. ej. por configuración
  externa), el test debe declararlo explícitamente con `@Import(...)` en vez de asumir que `@WebMvcTest` lo
  carga.

## Fallo 2 — El `Location` del `201` apuntaba a una URL corrupta
- **Síntoma**: una vez resuelto el Fallo 1, `test3` (ahora `create_conBodyValido_...`) fallaba comparando el
  header `Location` esperado (`http://localhost/api/suppliers/SUP-001`).
- **Causa raíz**: `ServletUriComponentsBuilder.fromCurrentRequest().path("{id}")` concatena el literal
  `"{id}"` directamente al path actual (`/api/suppliers`) sin separador, generando la plantilla
  `/api/suppliers{id}`. Al expandir con `SUP-001` el resultado era `/api/suppliersSUP-001` (sin `/`).
- **Corrección**: cambiar `.path("{id}")` por `.path("/{id}")` para que el separador quede explícito.
- **Prevención**: cualquier construcción de URI con `UriComponentsBuilder` debe verificarse con un test que
  compare el header completo (no solo el status), como ya hacía este test.

## Fallo 3 — La validación del DTO nunca se ejecutaba
- **Síntoma**: con un `name` vacío, la API no devolvía `400 VALIDATION_ERROR` sino un error inesperado
  (el mapeo fallaba más adelante al intentar usar un `Supplier` corrupto).
- **Causa raíz**: `CreateSupplierRequest` declara `@NotBlank`, `@Pattern` y `@Email`, pero el método
  `create` del controller recibía el `@RequestBody` **sin** `@Valid`. Sin esa anotación, Spring nunca activa
  Bean Validation sobre el DTO y el `MethodArgumentNotValidException` jamás se lanza.
- **Corrección**: agregar `@Valid` delante de `@RequestBody CreateSupplierRequest request`.
- **Prevención**: todo endpoint de escritura que reciba un DTO con anotaciones de validación debe llevar
  `@Valid`/`@Validated`; un test de "body inválido → 400" como el que ya existía es la red de seguridad que
  detecta su ausencia.

## Fallo 4 — `DELETE` devolvía `200` en vez de `204`
- **Síntoma**: `test5` (ahora `delete_proveedorExistente_...`) esperaba `isNoContent()` (204) y el
  controller respondía `isOk()` (200).
- **Causa raíz**: el método `delete` construía la respuesta con `ResponseEntity.ok().build()`. Un `DELETE`
  exitoso sin cuerpo de respuesta es semánticamente un `204 No Content`, no un `200 OK`.
- **Corrección**: `ResponseEntity.noContent().build()`.
- **Prevención**: en los endpoints `DELETE` sin body, usar siempre `204` salvo que el contrato de la API
  documente explícitamente otro código; un test de status explícito (como ya existía) detecta la
  desviación.

## Refactors aplicados

| Principio / práctica | Antes | Después | Test que lo respalda |
|---|---|---|---|
| DIP (controller → servicio) | `SupplierController` dependía de `SupplierServiceImpl` (clase concreta) | Se extrajo la interfaz `SupplierService`; el controller depende solo de ella | `SupplierControllerTest` (usa `@MockBean SupplierService`) |
| DIP (servicio → repositorio) | `SupplierServiceImpl` instanciaba `new InMemorySupplierRepository()` | Se inyecta `SupplierRepository` por constructor; `InMemorySupplierRepository` pasa a ser `@Repository` | `SupplierServiceImplTest` |
| SRP / dominio protege invariantes | `Supplier` era una clase mutable sin validaciones (setters libres) | `Supplier` es un `record` inmutable que valida nombre/NIT/correo y expone `register()`/`withId()` | `SupplierTest` |
| SRP (normalización fuera del controller) | El controller hacía `trim()/toUpperCase()` y limpiaba el NIT | La normalización vive en `Supplier.register(...)` (dominio); el controller solo traduce HTTP | `SupplierTest` (unitario de dominio) + `SupplierControllerTest` (verifica el resultado end-to-end) |
| Seguridad del manejo de errores | `handleUnexpected` devolvía `exception.toString()` al cliente (fuga de detalles internos/stack) | Se registra el error con `Logger` y se responde un mensaje genérico `"Ocurrió un error inesperado"` | Cubierto implícitamente por el resto de tests del controller (ningún caso de la suite ejercita ya una excepción no mapeada) |
| Diseño web (mapper sin bean) | `SupplierWebMapper` era `@Component` | Clase final con constructor privado y métodos estáticos | `SupplierControllerTest` (el slice ya no depende de un bean innecesario) |
| Tests con nombres descriptivos / sin String crudo | `test1`…`test5`; `test1` comparaba el body completo como `String` | Nombres que documentan comportamiento (`findById_conProveedorExistente_devuelve200ConSuBody`, etc.); `jsonPath` para cada campo | `SupplierControllerTest` |
| Verificación de interacción con el servicio | El test de body inválido no comprobaba que el servicio nunca se invocara | Se agregó `verify(supplierService, never()).create(any())` | `create_conNombreVacio_devuelve400YNuncaInvocaElServicio` |

## Bonus implementado
Se agregó `ArchitectureTest` (ArchUnit) que falla la build si alguna clase de `web` llega a depender de una
clase cuyo nombre termine en `ServiceImpl`, evitando que el DIP recién corregido se rompa en el futuro.
