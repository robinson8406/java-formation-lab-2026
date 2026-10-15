# Estrategia de testing — Catálogo de productos

> Completa esta plantilla. Es el entregable que diferencia el nivel **Experto**: no basta con que los
> tests pasen, hay que justificar **qué se prueba dónde y por qué**.

## 1. Pirámide propuesta

| Nivel | Herramienta | Qué valida | Qué NO valida | Nº de tests | Tiempo aprox. |
|-------|-------------|------------|---------------|-------------|---------------|
| Unitario (dominio) | JUnit 5 | Normalización e invariantes de `Product` | Serialización, wiring y seguridad HTTP | 1 | Milisegundos |
| Unitario (aplicación) | JUnit 5 + Mockito | IDs, uso del puerto, resultados y producto inexistente | Comportamiento real del adaptador en memoria | 2 | Milisegundos |
| Adaptador en memoria | JUnit 5 | Guardado, consulta y eliminación | Persistencia externa y transacciones | 1 | Milisegundos |
| Slice web | `@WebMvcTest` + `MockMvc` | Contrato HTTP, validación, errores, headers y reglas de acceso | Persistencia y wiring completo | 7 | Menos de 3 s (criterio medido abajo) |
| Integración | `@SpringBootTest` | Wiring de Spring, autenticación y flujo contra el repositorio real | Carga, concurrencia y persistencia externa | 1 | Segundos |

## 2. Matriz de decisión

La `X` identifica el nivel que verifica principalmente la regla; las comprobaciones
complementarias no sustituyen esa evidencia principal.

| Regla / comportamiento | Dominio | Aplicación | Slice web | Integración | Justificación |
|------------------------|:-------:|:----------:|:---------:|:-----------:|---------------|
| El precio debe ser > 0 | X | | X | | El dominio protege invariantes aunque se invoque el caso de uso fuera de HTTP; la validación web evita aceptar un request inválido. |
| El nombre vacío devuelve 400 | | | X | | Es una regla del contrato de entrada HTTP y se traduce a `VALIDATION_ERROR`. |
| Producto inexistente devuelve 404 | | X | X | X | La aplicación expresa ausencia con `ProductNotFoundException`; el slice comprueba su traducción HTTP y el smoke test pasa por el wiring real. |
| Se genera un id al crear | | X | X | X | El servicio asigna el identificador y se comprueba tanto en aislamiento como en el flujo integrado. |
| `Location` apunta al recurso creado | | | X | X | Es una obligación HTTP del `POST`, comprobada por el slice; la integración usa el header para consultar el recurso. |
| `DELETE` exige rol `ADMIN` | | | X | X | El slice prueba `403` con `EDITOR` y la integración ejecuta DELETE autenticado como `ADMIN`. |
| Las capas quedan cableadas | | | | X | Un único `@SpringBootTest` cubre controller, servicio, repositorio, seguridad y configuración. |

## 3. Decisiones y trade-offs

- `@MockBean` reemplaza el puerto `ProductService` dentro del contexto web y mantiene el foco en el controller, los filtros y el contrato HTTP. `@Mock` sustituye `ProductRepository` en el test unitario del servicio, sin arrancar Spring.
- Mantener los siete casos HTTP en un solo slice y limitar `@SpringBootTest` a un smoke test reduce la cantidad de contextos completos y localiza los fallos con rapidez. Los tiempos observados se registran en la tabla siguiente; no se extrapolan como garantía para cualquier agente del pipeline.
- Spring reutiliza el contexto cuando las clases solicitan una configuración equivalente. Cambios en propiedades, clases importadas, mocks o anotaciones de configuración pueden crear una clave de contexto diferente e invalidar esa reutilización.
- Riesgos pendientes: el adaptador es en memoria y no valida persistencia real; no hay pruebas de concurrencia ni rendimiento/carga; no se generan contratos para consumidores. En producción se añadirían tests de integración del adaptador con la base real, pruebas de contrato y carga acotadas. Las contraseñas locales de ejemplo de test se mantienen en configuración de test, no en configuración de ejecución.

## 4. Mediciones

| Comando | Tests | Tiempo reportado por Surefire |
|---------|-------|-------------------------------|
| `mvn -pl ejercicio-2-construccion test -Dtest='ProductControllerTest*'` | 7 | 6.229 s en ejecución aislada en frío (incluye arranque de Spring; no cumple el umbral en esta máquina) |
| `mvn -pl ejercicio-2-construccion verify` | 12 | `ProductControllerTest`: 1.108 s con el proceso/contexto ya inicializado; total Maven del módulo: 10.445 s |

El objetivo de `ProductControllerTest` es un tiempo menor a 3 segundos. En la corrida aislada
el arranque frío de Spring elevó el reporte a 6.229 s; en la corrida completa, Surefire reportó
1.108 s para esa clase. La duración de los siete casos con contexto caliente sí está bajo el
objetivo, pero el primer arranque no lo está en este entorno Windows; el tiempo total de Maven
no debe confundirse con el tiempo reportado por Surefire para la clase.

La verificación del agregador (`mvn -f sessions/week-08-webmvctest/pom.xml verify`) ejecutó
23 tests entre ambos ejercicios sin fallos; su duración total de Maven fue 19.706 s.
