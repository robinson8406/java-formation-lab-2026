# Ejercicio 2 — Construcción: catálogo de productos

> **Tipo**: construir desde cero. Solo tienes `ProductCatalogApplication.java` (el `main`) y el `pom.xml`.

## Contexto

El `ProductController` del catálogo debe reescribirse con una arquitectura limpia y con tests rápidos.
Los tests actuales del equipo usan `@SpringBootTest` y hacen que la fase de test del pipeline tarde 4 minutos.
Esta vez el servicio nace bien diseñado y con una **estrategia de testing explícita**.

## Contrato de la API (no negociable)

| Método | Ruta | Seguridad | Respuesta OK | Errores |
|--------|------|-----------|--------------|---------|
| `GET` | `/api/products/{id}` | pública | `200` + `ProductResponse` | `404 PRODUCT_NOT_FOUND` |
| `POST` | `/api/products` | HTTP Basic, rol `EDITOR` o `ADMIN` | `201` + header `Location` + `ProductResponse` | `400 VALIDATION_ERROR`, `401` |
| `DELETE` | `/api/products/{id}` | HTTP Basic, rol `ADMIN` | `204 No Content` | `404 PRODUCT_NOT_FOUND`, `401`, `403` |

**Request** `POST /api/products`:

```json
{ "name": "Laptop", "price": 3500000.00, "stock": 10 }
```

| Campo | Regla |
|-------|-------|
| `name` | obligatorio, no vacío, máximo 120 caracteres |
| `price` | obligatorio, mayor que 0 (usar `BigDecimal`, nunca `double` para dinero) |
| `stock` | obligatorio, mayor o igual que 0 |

**Response** `ProductResponse`: `{ "id": "PRD-0001", "name": "Laptop", "price": 3500000.00, "stock": 10 }`

**Error** (mismo formato que week-06):

```json
{
  "timestamp": "2026-10-08T15:00:00Z",
  "status": 404,
  "code": "PRODUCT_NOT_FOUND",
  "errors": ["Producto no encontrado: PRD-9999"],
  "path": "/api/products/PRD-9999"
}
```

Usuarios (credenciales externalizadas en `application.properties` con variables de entorno, como en week-07):

| Usuario | Roles |
|---------|-------|
| `editor` | `EDITOR` |
| `admin` | `EDITOR`, `ADMIN` |

## Arquitectura esperada (hexagonal / limpia)

```
com.indra.catalog.products
├── ProductCatalogApplication.java   ← ya existe
├── domain/
│   ├── model/        → Product (inmutable, protege sus invariantes)
│   ├── exception/    → ProductNotFoundException
│   └── port/         → ProductRepository (puerto de salida)
├── application/      → ProductService (interfaz = puerto de entrada) + implementación
├── infrastructure/
│   ├── persistence/  → adaptador en memoria del ProductRepository
│   └── security/     → configuración de Spring Security
└── web/              → ProductController, dto/, manejo de errores
```

Reglas de dependencia:

- `domain` no depende de nada (ni de Spring, ni de `web`, ni de `infrastructure`).
- `application` depende solo de `domain`. **No** recibe DTOs web.
- `web` depende de `application` a través de la **interfaz** `ProductService`.
- `infrastructure` implementa los puertos del `domain`.
- Sin lógica de negocio en el controller: solo traduce HTTP ↔ caso de uso.

## Lo que debes implementar

1. El código de producción completo según el contrato y la arquitectura.
2. `ProductControllerTest` con `@WebMvcTest(ProductController.class)` + `@MockBean ProductService`:
   - `GET` existente → `200` + `jsonPath` + `Content-Type`
   - `GET` inexistente → `404`
   - `POST` válido → `201` + header `Location`
   - `POST` inválido (nombre vacío) → `400` con mensaje de error
   - `DELETE` → `204`
   - Seguridad: `POST` anónimo → `401`, `DELETE` con `editor` → `403`
3. Tests unitarios **sin Spring** para el servicio y el modelo de dominio (mínimo 1 test por clase nueva).
4. Como máximo **un** `@SpringBootTest` (smoke test que valide el cableado completo).
5. `ESTRATEGIA_TESTING.md` completo (plantilla en esta carpeta).

## Cómo ejecutarlo

```bash
cd sessions/week-08-webmvctest
mvn -pl ejercicio-2-construccion verify
# Medición del criterio no funcional:
mvn -pl ejercicio-2-construccion test -Dtest='ProductControllerTest*'
```

Para iniciar la aplicación, define `PRODUCT_EDITOR_PASSWORD` y `PRODUCT_ADMIN_PASSWORD`
en el entorno. No hay contraseñas de ejecución por defecto; las pruebas usan credenciales
aisladas definidas en sus propiedades de test.

> ⚠️ Con Spring Security en el classpath, `@WebMvcTest` aplica la seguridad por defecto, **no** la tuya,
> a menos que la importes. Entender por qué es parte del reto.
