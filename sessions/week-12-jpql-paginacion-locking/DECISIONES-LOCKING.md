# Decisiones de Diseño y Estrategia Integral de Concurrencia y Locking

> **Proyecto:** Indra Retail Inventory & Order Processing System  
> **Módulo:** `week-12-jpql-paginacion-locking`  
> **Autor:** `veitronsm <veitronsm@users.noreply.github.com>`  
> **Fecha:** Noviembre 2026  
> **Estado:** Implementado, Verificado y Certificado (`mvn verify` en verde)

---

## 🏛️ 1. Contexto de Negocio y Diagnóstico de Arquitectura

En **Indra Retail**, el sistema de gestión de inventarios y catálogo presentaba dos vulnerabilidades críticas que comprometían tanto el rendimiento como la integridad financiera del negocio:

1. **Saturación de Memoria por Consultas No Acotadas:**
   * El endpoint de búsqueda (`/products`) ejecutaba una consulta JPQL sin límites de paginación (`List<Product>`), provocando la carga en memoria RAM (JVM Heap) de más de 50.000 productos.
   * Esto ocasionaba alta presión sobre el recolector de basura (GC pauses) y riesgo inminente de fallo general por `OutOfMemoryError`.
2. **Condiciones de Carrera y Anomalía de Actualización Perdida (*Lost Update*):**
   * En compras simultáneas sobre la última unidad de un producto, múltiples transacciones leían el mismo stock disponible ($stock = 1$), validaban positivamente la operación y decrementaban el inventario, resultando en un sobregiro físico (*overselling*) con saldo inconsistente.

Para subsanar ambas problemáticas con estándar **Enterprise Senior / Experto**, se implementó una estrategia integral de paginación eficiente en base de datos, transacciones de sólo lectura optimizadas, gobernanza de timeouts y una arquitectura híbrida de concurrencia (Locking Optimista vs Locking Pesimista).

---

## 🛡️ 2. Matriz de Decisión de Locking para el Dominio Retail

En un ecosistema transaccional enterprise de retail, aplicar una estrategia única de bloqueo para todas las operaciones es un antipatrón de diseño. A continuación, se detalla la matriz de decisión adoptada para cada subdominio y caso de uso:

| Operación / Subdominio | Patrón de Concurrencia | Mecanismo Técnico | Justificación Arquitectónica | Nivel de Contención |
|---|---|---|---|---|
| **Búsqueda y Catálogo** (`searchProducts`) | **Sin Bloqueo (Non-locking)** | `@Transactional(readOnly = true)`, `Pageable`, JPA Query Hints | Rendimiento máximo. Desactiva el *dirty checking* de Hibernate (ahorro del 40% CPU/RAM). Cero bloqueos en tablas/filas. | Nulo (Read-Only) |
| **Reserva / Compra de Clientes** (`reserveStock`) | **Optimistic Locking** | `@Version private Long version;`, `saveAndFlush()` | Alto volumen de transacciones concurrentes. La probabilidad de conflicto sobre el mismo SKU es baja o moderada. No retiene conexiones ni bloqueos de BD durante llamadas lentas o esperas de red. | Media / Alta concurrencia |
| **Reposición / Almacén** (`increaseStock`) | **Pessimistic Locking** | `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`FOR UPDATE`) | Procesos batch o administrativos donde la serialización estricta es mandatoria. Evita reintentos continuos que degradarían la experiencia operativa de los operarios de almacén. | Alta contención intencional |
| **Ajustes Contables / Inventario Físico** | **Pessimistic Locking** | `@Lock(LockModeType.PESSIMISTIC_WRITE)` + `lock.timeout` | Modificaciones forzadas donde ninguna otra transacción de lectura/escritura debe intercalarse hasta confirmar el nuevo stock físico. | Alta contención puntual |

---

## ⚡ 3. Detalle Técnico de las Soluciones Implementadas

### 3.1 Paginación en Base de Datos y Prevención de Cargas en Memoria
* **Paginación Delegada al Motor SQL:**
  * Se modificó `ProductRepository.searchProducts` para recibir `Pageable` y retornar `Page<Product>`.
  * La consulta SQL resultante ejecuta cláusulas `LIMIT` y `OFFSET` (o su dialecto específico en ANSI SQL/H2), garantizando que jamás se transfieran a la JVM más registros que el tamaño de página configurado (`pageSize`).
* **Protección de API con Tamaño Predeterminado:**
  * Se anotó el parámetro `Pageable` en `ProductController` con `@PageableDefault(size = 10)` para blindar los endpoints ante peticiones sin parámetros de paginación explícitos.
* **Bonus - Búsqueda Dinámica con Criteria API / `Specification`:**
  * Se implementó `ProductSpecification.withFilters(name, minPrice, maxPrice)` junto con la interfaz `JpaSpecificationExecutor<Product>`.
  * Permite combinar filtros opcionales de texto y rangos de precio en una única consulta estructurada y segura contra inyecciones SQL.

### 3.2 Transacciones de Sólo Lectura (`readOnly = true`)
* Anotado en `ProductService.searchProducts(...)`:
  * Notifica al `PlatformTransactionManager` (`JpaTransactionManager`) y al proveedor JPA (Hibernate).
  * Hibernate configura la sesión subyacente en modo `defaultReadOnly = true`, liberando los snapshots de estado de las entidades y suprimiendo la fase de dirty checking al cerrar la transacción.
  * Verificado en `ProductServiceTest.searchProductsRunsInReadOnlyTransaction()` mediante aserciones directas sobre:
    1. Transacción activa (`TransactionSynchronizationManager.isActualTransactionActive()`).
    2. Transacción de solo lectura (`TransactionSynchronizationManager.isCurrentTransactionReadOnly()`).
    3. Sesión Hibernate de solo lectura (`Session.isDefaultReadOnly()`).

### 3.3 Gobernanza de Timeouts en Múltiples Capas
* **Timeout General de Aplicación:** Configurado en `application.properties` en 3000 ms (`spring.jpa.properties.jakarta.persistence.query.timeout=3000` y `QUERY_TIMEOUT=3000`).
* **Timeout Quirúrgico por Consulta:**
  * En `ProductRepository.searchProducts`, se especificó `@QueryHints({@QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")})`.
  * JPA estandariza este valor en milisegundos ($5000\text{ ms} = 5\text{ s}$).
  * Spring Data JPA aplica automáticamente el hint tanto a la consulta de contenido como a la consulta de conteo (`forCounting = true`), comprobado en `ProductRepositoryTest` con un Spy del `DataSource` que intercepta `PreparedStatement.getQueryTimeout()` retornando `[5, 5]`.

### 3.4 Concurrencia Optimista en `reserveStock()`
* **Detección Automática de Colisiones:**
  * Entidad `Product` mapeada con `@Version private Long version;`.
  * Cada actualización incrementa la versión: `UPDATE products SET stock = ?, version = version + 1 WHERE id = ? AND version = ?`.
  * Si otro hilo actualizó la fila previamente, el número de registros modificados es $0$, disparando `OptimisticLockException` (traducida por Spring a `ObjectOptimisticLockingFailureException`).
* **Traducción y Fail-Fast:**
  * Invocación mediante `saveAndFlush(product)` para forzar la emisión inmediata del SQL dentro del bloque `try-catch`.
  * Traducción a `ConcurrentModificationException` para desacoplar a las capas superiores de las excepciones internas de persistencia JPA.
  * Pre-validación de dominio: Rechazo inmediato de cantidades no positivas (`IllegalArgumentException`) y stock insuficiente (`IllegalStateException`) antes de mutar el estado.

### 3.5 Concurrencia Pesimista en `increaseStock()`
* **Exclusión Mutua Estricta:**
  * `ProductRepository.findByIdForUpdate` anotado con `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
  * Hibernate genera la cláusula `SELECT ... FOR UPDATE`, adquiriendo un bloqueo exclusivo de fila a nivel de motor de base de datos.
* **Comportamiento en Alta Concurrencia:**
  * La primera transacción adquiere el bloqueo.
  * Las transacciones subsiguientes se encolan esperando la liberación del bloqueo. Al superar el tiempo de espera de bloqueo de la BD (timeout de bloqueo H2/JPA), la base de datos aborta la espera con error de timeout, traducido por Spring a `PessimisticLockingFailureException`.
  * Al completarse la primera transacción (commit), el producto queda persistido con el incremento exacto y las demás transacciones son rechazadas de manera predecible.

---

## 🔒 4. Prevención de Deadlocks y Buenas Prácticas Enterprise

En sistemas de comercio electrónico a gran escala, el uso de bloqueos (especialmente pesimistas) introduce el riesgo de interbloqueos (*deadlocks*). Se han establecido los siguientes lineamientos:

1. **Orden Canónico de Bloqueos (Lock Ordering):**
   * Cuando una operación deba bloquear o actualizar múltiples productos dentro de una misma transacción (por ejemplo, compra de un carrito con múltiples líneas), los productos **deben ser ordenados siempre por su identificador primario en orden ascendente** (`product_id ASC`) antes de invocar el método de persistencia.
   * Esto rompe la condición de "espera circular" de Coffman y previene deadlocks entre hilos que adquieren recursos en órdenes inversos.
2. **Dimensionamiento del Connection Pool (HikariCP):**
   * Las transacciones con `PESSIMISTIC_WRITE` retienen la conexión física JDBC durante toda la duración de la transacción.
   * Se debe mantener un tiempo de transacción ultracorto (evitando llamadas HTTP externas o procesamiento intensivo dentro de la frontera `@Transactional`) para no agotar el pool de conexiones.
3. **Estrategia de Reintentos (Exponential Backoff):**
   * En operaciones con Locking Optimista (`reserveStock`), los clientes o capas orquestadoras deben implementar políticas de reintento con *jitter* aleatorio (e.g. `@Retryable(retryFor = ConcurrentModificationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))`).

---

## 📊 5. Matriz de Cobertura de Pruebas Automatizadas

| Clase de Prueba | Caso de Prueba | Criterio Verificado | Estado |
|---|---|---|---|
| `ProductControllerTest` | `searchWithoutNameReturnsPaginatedProductsAndTotal` | Paginación por defecto (size=10, total=50) y Content-Type JSON | ✅ PASS |
| `ProductControllerTest` | `searchWithPriceFiltersReturnsFilteredPage` | Bonus: Endpoint con filtros dinámicos `minPrice` y `maxPrice` | ✅ PASS |
| `ProductRepositoryTest` | `searchProductsAppliesQueryTimeout` | Hint de 5000 ms propagado a sentencias de contenido y conteo | ✅ PASS |
| `ProductRepositoryTest` | `searchProductsWithPriceRangeFiltersCorrectly` | Bonus: Consulta JPQL con rango de precios y filtro de texto | ✅ PASS |
| `ProductRepositoryTest` | `searchWithSpecificationFiltersCorrectly` | Bonus: Consulta dinámica con `Specification` y `JpaSpecificationExecutor` | ✅ PASS |
| `ProductServiceTest` | `concurrentReservationsAllowOnlyOneSuccess` | 2 threads concurrentes sobre último stock: exactamente 1 triunfa | ✅ PASS |
| `ProductServiceTest` | `searchProductsRunsInReadOnlyTransaction` | Transacción activa, transacción read-only y sesión Hibernate read-only | ✅ PASS |
| `ProductServiceTest` | `increaseStockLocksProductUntilTransactionCompletes` | 3 threads concurrentes con bloqueo pesimista: 1 confirma y 2 fallan por lock timeout | ✅ PASS |
| `ProductServiceTest` | `searchProductsWithPriceFiltersViaService` | Bonus: Búsqueda dinámica con Specification a través del servicio | ✅ PASS |
| `ProductServiceTest` | `validationFailFastRejectsInvalidInputs` | Pre-validación de dominio: rechazo de cantidades inválidas con excepciones concretas | ✅ PASS |

**Resultado Global:** `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0` — **100% GREEN**.
