# Estrategia de testing — Catálogo de productos

> Documento de justificación técnica y estrategia de pirámide de pruebas para el catálogo de productos.
> Define qué se prueba en cada nivel arquitectónico, por qué y con qué herramientas.

## 1. Pirámide propuesta

| Nivel | Herramienta | Qué valida | Qué NO valida | Nº de tests | Tiempo aprox. |
|-------|-------------|------------|---------------|:-----------:|:-------------:|
| Unitario (dominio) | JUnit 5 | Invariantes del modelo `Product` (nombre no nulo/vacío, longitud <= 120, precio > 0, stock >= 0), inmutabilidad, método `withId` y contratos `equals`/`hashCode`/`toString`. | Serialización JSON, endpoints HTTP, seguridad, inyección de dependencias de Spring, persistencia. | 11 | ~0.04 s |
| Unitario (aplicación) | JUnit 5 + Mockito | Lógica de negocio y orquestación del caso de uso (`ProductServiceImpl`): generación de identificador `PRD-xxxx`, preservación de IDs existentes, validación de parámetros nulos/vacíos y lanzamiento de `ProductNotFoundException`. | Filtros de Spring Security, mapeo web HTTP (códigos de estado, Location, DTOs), serialización/deserialización, persistencia real. | 15 | ~0.35 s |
| Slice web | `@WebMvcTest` + `MockMvc` | Capa web (`ProductController`): enrutamiento HTTP, códigos de estado (200, 201, 204, 400, 401, 403, 404, 500), header `Location`, Bean Validation (`@Valid`), `ApiExceptionHandler` y reglas de autorización de Spring Security (`SecurityConfig`). | Lógica interna de casos de uso de negocio (mockeada con `@MockBean`), persistencia en repositorio, cableado de beans de servicios e infraestructura completos. | 9 | ~1.6 s |
| Integración | `@SpringBootTest` | Cableado integral del `ApplicationContext` de Spring Boot: resolución de dependencias, descubrimiento de componentes, autoconfiguración de seguridad y validación de instanciación de beans principales (smoke test). | Escenarios exhaustivos de validación de entradas de usuario, combinatoria de casos borde de dominio, latencia de red real. | 1 | ~1.2 s |

## 2. Matriz de decisión

Para cada regla, se define en qué nivel se prueba y su justificación técnica:

| Regla / comportamiento | Dominio | Aplicación | Slice web | Integración | Justificación |
|------------------------|:-------:|:----------:|:---------:|:-----------:|---------------|
| El precio debe ser > 0 | **X** | | **X** | | En dominio se valida como invariante fundamental de la entidad `Product` (`IllegalArgumentException`). En el slice web se valida en el borde HTTP vía Bean Validation (`@Positive` en `CreateProductRequest`) para rechazar tempranamente solicitudes inválidas con HTTP 400 y código `VALIDATION_ERROR`. |
| El nombre vacío devuelve 400 | | | **X** | | Es una responsabilidad de la capa de transporte web: Bean Validation intercepta el body antes de entrar al servicio y `ApiExceptionHandler` transforma `MethodArgumentNotValidException` en una respuesta HTTP 400 con código `VALIDATION_ERROR` estructurado. |
| Producto inexistente devuelve 404 | | **X** | **X** | | En aplicación se valida que al no encontrar el ID en `ProductRepository` se lance la excepción de dominio `ProductNotFoundException`. En el slice web se valida que `ApiExceptionHandler` capture dicha excepción y retorne HTTP 404 con código `PRODUCT_NOT_FOUND` y payload adecuado. |
| Se genera un id al crear | | **X** | | | La generación y asignación del identificador único del producto con formato `PRD-xxxx` es una responsabilidad de la capa de aplicación (`ProductServiceImpl`), verificada de manera rápida y sin sobrecarga de framework mediante Mockito y `ArgumentCaptor`. |
| `Location` apunta al recurso creado | | | **X** | | El armado del encabezado HTTP `Location` con la URI absoluta del recurso recién creado (`ServletUriComponentsBuilder`) es exclusivo de la capa web y del protocolo HTTP, por lo que pertenece al slice test con `MockMvc`. |
| `DELETE` exige rol `ADMIN` | | | **X** | | La autorización basada en roles (Spring Security `SecurityFilterChain`) opera en los filtros web de la petición HTTP. El slice web con `@WithMockUser` valida de forma aislada que un usuario anónimo reciba 401, un `EDITOR` reciba 403 y un `ADMIN` reciba 204. |
| Las capas quedan cableadas | | | | **X** | | Validar que el contenedor IoC de Spring Boot arranque, resuelva todas las inyecciones de dependencias por constructor sin referencias circulares y cargue el árbol completo de beans requiere un smoke test con `@SpringBootTest`. |

## 3. Decisiones y trade-offs

- **¿Por qué `@MockBean` en el slice test y `@Mock` en el test del servicio?**
  - `@Mock` de Mockito crea un mock de Java plano en memoria sin necesidad de contenedor IoC ni contexto de Spring. En el test unitario de `ProductServiceImplTest`, la suite corre en milisegundos sin levantar ningún bean ni ciclo de vida de Spring.
  - `@MockBean` es una anotación de Spring Boot Test que intercepta el `ApplicationContext` del slice (`@WebMvcTest`). Reemplaza o registra dinámicamente un bean simulado de Mockito dentro del ApplicationContext de Spring para que el `ProductController` bajo prueba reciba este mock inyectado por el contenedor de Spring.

- **¿Qué pasa con el tiempo del pipeline si cada test de controller usa `@SpringBootTest`? (cita tus mediciones)**
  - Levantar `@SpringBootTest` para cada clase de controller fuerza la inicialización de todo el árbol de beans, autoconfiguraciones, listeners y capa de persistencia. En suites grandes, si cada test usa configuraciones ligeramente distintas o ensucia el contexto, el tiempo se multiplica exponencialmente alcanzando varios minutos (como los 4 minutos mencionados en el contexto del ejercicio).
  - En contraste, con `@WebMvcTest` + `@Import(SecurityConfig.class)`, el tiempo reportado por Surefire para los 9 tests de `ProductControllerTest` fue de tan solo 1.614 s, manteniendo la retroalimentación inmediata en el ciclo de desarrollo y en CI.

- **¿Cómo aprovechas el caché de contexto de Spring entre clases de test? ¿Qué lo invalida?**
  - Spring TestContext Framework almacena en caché los `ApplicationContext` creados basándose en una clave compuesta (`merged context configuration`: clases de configuración, perfiles activos, propiedades, anotaciones). Si múltiples clases de test comparten exactamente la misma configuración, el contexto se reutiliza en milisegundos.
  - Lo invalidan: el uso de `@DirtiesContext`, cambios en perfiles activos (`@ActiveProfiles`), combinaciones dispares de `@MockBean` / `@SpyBean` (ya que cada conjunto diferente de mocks genera una configuración de contexto distinta que no puede ser compartida con tests que no tienen dichos mocks o tienen mocks distintos), y sobreescrituras dinámicas de propiedades con `@TestPropertySource`.

- **¿Qué riesgos quedan sin cubrir y cómo los cubrirías (contract tests, tests de carga, etc.)?**
  - **Riesgos sin cubrir y su mitigación**:
    1. *Desalineación del contrato con clientes consumidores (frontends u otros microservicios)*: se cubre con Consumer-Driven Contract Tests mediante Spring Cloud Contract o Pact.
    2. *Concurrencia real y contención en almacenamiento bajo estrés*: se cubre con pruebas de rendimiento y carga con k6 o Gatling.
    3. *Integración real con base de datos de producción (migraciones flyway/liquibase, dialectos SQL, restricciones de claves foráneas)*: se cubre con integration tests con Testcontainers (PostgreSQL/MySQL en contenedor Docker real).
    4. *Compatibilidad de red, proxys inversos y serialización TLS*: se cubre con End-to-End (E2E) tests en staging.

## 4. Mediciones

| Comando | Tests | Tiempo reportado por Surefire |
|---------|:-----:|:-----------------------------:|
| `mvn -pl ejercicio-2-construccion test -Dtest='ProductControllerTest*'` | 9 | 1.614 s |
| `mvn -pl ejercicio-2-construccion verify` | 40 | 3.120 s |
