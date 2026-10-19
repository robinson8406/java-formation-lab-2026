# Decisiones de Mapeo JPA y Arquitectura de Persistencia — Nivel Experto

> Documento de fundamentación técnica para el dominio de **Indra Retail** (Semana 09: Mapeos JPA).
> Incluye el esquema DDL formal, matriz de cascadas y fetch types, análisis de ciclo de vida de Hibernate y mitigación de problemas de concurrencia y rendimiento en producción.

---

## 1. Esquema DDL Propuesto (ANSI SQL / PostgreSQL)

El siguiente esquema representa el contrato DDL formal derivado del modelo de entidades JPA, diseñado con restricciones de integridad referencial, tipos de datos precisos para finanzas e índices de optimización para consultas transaccionales.

```sql
-- 1. Tabla de Clientes (con Value Object Address embebido)
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    street VARCHAR(255),
    city VARCHAR(100),
    zip_code VARCHAR(20),
    CONSTRAINT uk_customers_email UNIQUE (email)
);

-- 2. Tabla de Catálogo de Productos (con versión para bloqueo optimista)
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    version BIGINT NOT NULL DEFAULT 0
);

-- 3. Tabla de Pedidos (Aggregate Root)
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    customer_id BIGINT NOT NULL,
    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers (id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_orders_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED'))
);

-- 4. Tabla de Ítems de Pedido (Entidad interna del agregado)
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE RESTRICT
);

-- ─────────────────────────────────────────────────────────────────────────────
-- Índices para Optimización Transaccional y Joins
-- ─────────────────────────────────────────────────────────────────────────────
CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
```

---

## 2. Matriz de Decisiones de Fetch Type (`LAZY` vs `EAGER`)

| Relación | Anotación | Fetch Type | Justificación Técnica |
|---|---|:---:|---|
| `Order` $\rightarrow$ `Customer` | `@ManyToOne` | `LAZY` | Evita la carga ansiosa del cliente en consultas masivas o reportes de pedidos. En JPA, por defecto `@ManyToOne` es `EAGER`, lo cual es un anti-patrón de rendimiento. |
| `Order` $\rightarrow$ `OrderItem` | `@OneToMany` | `LAZY` | Las colecciones de ítems solo deben cargarse en memoria cuando la lógica de negocio del pedido requiera operar con su detalle o calcular totales. Evita N+1 queries. |
| `OrderItem` $\rightarrow$ `Order` | `@ManyToOne` | `LAZY` | Evita consultas cíclicas o redundantes cuando se manipulan ítems individualmente en batch. |
| `OrderItem` $\rightarrow$ `Product` | `@ManyToOne` | `LAZY` | La consulta de ítems no debe arrastrar obligatoriamente los metadatos completos del catálogo de productos a menos que se invoque explícitamente. |

### Riesgos de `FetchType.EAGER` en Sistemas Empresariales:
1. **Problema N+1 Queries**: Si se consultan $N$ órdenes con `EAGER`, Hibernate ejecuta 1 consulta para las órdenes y $N$ consultas secundarias adicionales para traer cada entidad asociada.
2. **Multiplicación de Producto Cartesiano**: Al combinar múltiples relaciones `EAGER`, Hibernate genera JOINs masivos que duplican filas en memoria, provocando desbordamiento del Garbage Collector y latencias inaceptables (`MultipleBagFetchException`).
3. **Incapacidad de Paginar en BD**: Hibernate no puede aplicar `LIMIT` y `OFFSET` a nivel de base de datos cuando hay JOINs hacia colecciones `EAGER`, viéndose forzado a cargar todo el ResultSet en memoria y paginar en el heap de la JVM.

### ¿Por qué `spring.jpa.open-in-view=false` es Obligatorio?
* **OSIV (Open Session in View)** mantiene la sesión de Hibernate abierta durante toda la petición HTTP (incluyendo filtros y serialización JSON).
* Esto enmascara malas prácticas de diseño y provoca que se ejecuten consultas SQL durante la serialización del controlador, agotando rápidamente el pool de conexiones (HikariCP) bajo concurrencia.
* Con `open-in-view=false`, el contexto de persistencia termina estrictamente en la capa transaccional (`@Transactional`), forzando a que las consultas carguen sus dependencias de forma planificada (`JOIN FETCH`, `@EntityGraph` o Proyecciones DTO).

---

## 3. Matriz de Decisiones de Cascada (`CascadeType`)

| Relación | Cascada Configurada | `orphanRemoval` | Justificación de Negocio y Arquitectura |
|---|:---:|:---:|---|
| `Order` $\rightarrow$ `OrderItem` | `{PERSIST, MERGE}` | `true` | **Aggregate Root**: `Order` es la raíz del agregado. Al guardar o actualizar un pedido, sus ítems deben sincronizarse automáticamente. Si un ítem se remueve de la lista `items`, `orphanRemoval=true` garantiza su eliminación física en base de datos. Se evita `ALL` para no propagar operaciones peligrosas. |
| `Order` $\rightarrow$ `Customer` | **Ninguna** | `false` | Un pedido no gestiona el ciclo de vida del cliente. Crear un pedido no debe alterar ni borrar al cliente de la empresa. |
| `OrderItem` $\rightarrow$ `Product` | **Ninguna** | `false` | El producto pertenece al catálogo comercial independiente. Eliminar un pedido o sus ítems jamás debe eliminar el producto en inventario. |

---

## 4. Ciclo de Vida de Entidades JPA y `LazyInitializationException`

### Estados del Ciclo de Vida:
1. **Transient (Nuevo)**: Objeto instanciado en Java (`new Order()`), sin identificador persistente y no asociado a ningún contexto de persistencia (`EntityManager`).
2. **Managed / Persistent**: Entidad asociada a una sesión activa de Hibernate. Sus cambios se sincronizan automáticamente en el flush (`dirty checking`).
3. **Detached (Desconectado)**: Entidad que posee ID pero su sesión/transacción de origen ya se cerró. Las referencias `LAZY` no inicializadas quedan representadas por Proxies (`ByteBuddyInterceptor`).
4. **Removed**: Entidad marcada para su eliminación en BD durante el próximo flush (`entityManager.remove()`).

### Anatomía de `LazyInitializationException`:
Cuando el método transaccional finaliza (p. ej. `manager.findById(...)`), el `EntityManager` se cierra y la entidad pasa a estado **Detached**. Si el código cliente invoca un getter sobre una propiedad o colección LAZY (p. ej. `order.getCustomer().getName()`), el proxy intenta solicitar una sesión activa a Hibernate. Al no encontrarla (`no Session`), lanza:
`org.hibernate.LazyInitializationException: could not initialize proxy - no Session`.

### Formas Correctas de Resolverlo (Sin usar EAGER):
1. **Cargar dentro de la transacción activa**: Ejecutar la lógica de acceso dentro de un bloque `@Transactional` o `TransactionTemplate`.
2. **Consultas con `JOIN FETCH`**: En consultas JPQL personalizadas:
   ```sql
   SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH o.customer c WHERE o.id = :id
   ```
3. **JPA `@EntityGraph`**: Declarar grafos de entidades para indicar a Hibernate qué relaciones cargar en consultas específicas.
4. **Proyecciones DTO**: Seleccionar únicamente los campos requeridos en DTOs inmutables (Java Records), evitando el paso de entidades ricas desconectadas a capas superiores.

---

## 5. Control de Concurrencia y Bloqueo Optimista (`@Version`)

En entornos transaccionales de alto volumen, múltiples hilos pueden intentar modificar el stock de un `Product` simultáneamente.

* Se agregó el campo `@Version private Long version;` en la entidad [`Product`](file:///C:/Users/dfmunoz/Documents/java-formation-lab-2026/sessions/week-09-mapeos-jpa/src/main/java/com/indra/retail/entities/Product.java).
* En cada actualización, Hibernate genera:
  ```sql
  UPDATE products SET stock = ?, version = version + 1 WHERE id = ? AND version = ?
  ```
* Si dos transacciones leen la misma versión $0$ y la primera persiste, la versión en BD se incrementa a $1$. Cuando la segunda intenta el `UPDATE` con `WHERE version = 0`, no actualiza filas y Hibernate lanza inmediatamente `ObjectOptimisticLockingFailureException` (`OptimisticLockException`), previniendo condiciones de carrera (*Lost Updates*) sin necesidad de bloqueos pesados a nivel de fila (`SELECT FOR UPDATE`).

---

## 6. Value Objects Embebidos (`@Embeddable`)

Para evitar la creación innecesaria de tablas y claves primarias artificiales cuando un concepto no tiene identidad independiente:
* Se implementó [`Address`](file:///C:/Users/dfmunoz/Documents/java-formation-lab-2026/sessions/week-09-mapeos-jpa/src/main/java/com/indra/retail/entities/Address.java) como `@Embeddable` dentro de [`Customer`](file:///C:/Users/dfmunoz/Documents/java-formation-lab-2026/sessions/week-09-mapeos-jpa/src/main/java/com/indra/retail/entities/Customer.java) (`@Embedded`).
* Esto almacena las columnas `street`, `city`, `zip_code` en la misma tabla `customers`, mejorando la localidad de referencia, simplificando los joins y manteniendo el modelo de dominio enriquecido y cohesivo.
