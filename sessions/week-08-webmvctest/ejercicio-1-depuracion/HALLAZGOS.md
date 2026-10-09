# Diagnóstico y Refactor — Ejercicio 1 (Catálogo de Proveedores)

## Fallo 1 — Bean `SupplierWebMapper` no disponible en el contexto del slice test
- **Síntoma**: `IllegalStateException: Failed to load ApplicationContext` al ejecutar `SupplierControllerTest`. Error: `NoSuchBeanDefinitionException: No qualifying bean of type 'com.indra.catalog.suppliers.web.SupplierWebMapper' available`.
- **Causa raíz**: La anotación `@WebMvcTest(SupplierController.class)` es un *test slice* que optimiza los tiempos de arranque limitando el escaneo de componentes exclusivamente a la capa web (`@Controller`, `@RestController`, `@ControllerAdvice`, `WebMvcConfigurer`, `Filter`, etc.). Los componentes genéricos anotados con `@Component`, como `SupplierWebMapper`, son deliberadamente excluidos del escaneo automático. Al requerir `SupplierController` este mapper por constructor y no encontrarlo en el contexto, el arranque falla.
- **Corrección**: Se incluyó explícitamente `@Import(SupplierWebMapper.class)` en `SupplierControllerTest`. Esto registra la instancia real del mapper en el slice context sin necesidad de levantar componentes innecesarios de infraestructura o persistencia ni recurrir a `@SpringBootTest`.
- **Prevención**: Documentar y verificar los límites del slice `@WebMvcTest`. Si un controlador colabora con helpers o mappers anotados con `@Component`, deben importarse con `@Import` o simularse con `@MockBean`.

## Fallo 2 — Concatenación errónea de ruta en el header `Location`
- **Síntoma**: Fallo de aserción en `test3`: `Response header 'Location' expected:<http://localhost/api/suppliers/SUP-001> but was:<http://localhost/api/suppliersSUP-001>`.
- **Causa raíz**: En `SupplierController.create`, la construcción de la URI mediante `ServletUriComponentsBuilder.fromCurrentRequest().path("{id}")` omite el delimitador de barra `/`. Dado que la ruta actual termina en `/api/suppliers`, concatenar `{id}` directamente sin barra produce `/api/suppliersSUP-001`.
- **Corrección**: Se corrigió la plantilla de la ruta a `.path("/{id}")`, asegurando la generación canónica de la URI del recurso creado (`http://localhost/api/suppliers/SUP-001`).
- **Prevención**: Validar siempre el formato RFC 7231 del header `Location` en las pruebas de creación (`201 Created`), garantizando que la URL del recurso nuevo sea completamente navegable.

## Fallo 3 — Omisión de la anotación `@Valid` en el endpoint de creación
- **Síntoma**: Fallo en `test4` con payload inválido (nombre vacío): `Status expected:<400> but was:<500>`.
- **Causa raíz**: `CreateSupplierRequest` define restricciones de Bean Validation (`@NotBlank`, `@Pattern`, `@Email`), pero el parámetro del método `create` en `SupplierController` carecía de la anotación `@Valid`. Como consecuencia, Spring MVC no activó el validador de argumentos y continuó con la ejecución normal, provocando un error inesperado al intentar procesar un objeto en estado inválido.
- **Corrección**: Se agregó `@Valid` al parámetro `@RequestBody CreateSupplierRequest request` en `SupplierController.java`. Esto activa la intercepción de Spring y el lanzamiento de `MethodArgumentNotValidException`, que es capturada por `ApiExceptionHandler` retornando HTTP 400.
- **Prevención**: Incluir siempre pruebas negativas con payloads inválidos en los tests de controladores y verificar que el status resultante sea `400 Bad Request`.

## Fallo 4 — Código de estado HTTP incorrecto en eliminación (`DELETE`)
- **Síntoma**: Fallo de aserción en `test5`: `Status expected:<204> but was:<200>`.
- **Causa raíz**: El estándar REST para operaciones de eliminación que no retornan cuerpo en la respuesta establece el uso del código `204 No Content`. `SupplierController.delete` retornaba `ResponseEntity.ok().build()` (`200 OK`).
- **Corrección**: Se modificó el retorno del método `delete` a `ResponseEntity.noContent().build()`.
- **Prevención**: Seguir la semántica RESTful para operaciones idempotentes sin contenido de respuesta (`204 No Content`).

---

## Refactors Aplicados (Nivel Senior / Experto)

| Principio / Práctica | Antes | Después | Test que lo respalda |
|---|---|---|---|
| **DIP (Dependency Inversion)** | `SupplierController` y `SupplierControllerTest` dependían directamente de la clase concreta `SupplierServiceImpl`. | Se extrajo la interfaz `SupplierService`. El controlador y los mocks dependen de la abstracción. | `SupplierControllerTest` con `@MockBean private SupplierService supplierService;`. |
| **DIP en Capa de Servicio** | `SupplierServiceImpl` instanciaba directamente `new InMemorySupplierRepository()`. | `SupplierServiceImpl` recibe `SupplierRepository` por constructor y `InMemorySupplierRepository` se anotó con `@Repository`. | Compilación desacoplada e inyección transparente en Spring. |
| **SRP y Dominio con Invariantes** | `SupplierController` contenía lógica de normalización de datos (mayúsculas, eliminación de puntos en NIT). | La lógica de normalización se encapsuló en el método `normalize()` de la entidad `Supplier` y se invoca desde el servicio. El controller solo traduce HTTP. | `SupplierTest.normalize_normalizesNameAndTaxId()`. |
| **Seguridad y Hardening de API** | `ApiExceptionHandler.handleUnexpected` exponía `exception.toString()` al cliente en errores 500. | Se reemplazó por un mensaje genérico seguro: `"Ha ocurrido un error interno en el servidor"`, registrando el error real en logs con SLF4J. | Inspección de respuestas seguras sin fugas de stack trace. |
| **Calidad de Pruebas** | Tests nombrados `test1` a `test5` y aserción de payload con `content().string(...)`. | Nombres autodescriptivos según convención BDD/escenario y aserciones atómicas y robustas con `jsonPath`. | `SupplierControllerTest` (todos los métodos refactorizados). |
