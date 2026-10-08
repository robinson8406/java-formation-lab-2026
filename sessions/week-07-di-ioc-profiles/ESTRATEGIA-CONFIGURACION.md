# Propuesta de Estrategia de Configuración para Indra Notifications (Nivel Experto)

Esta propuesta formaliza la arquitectura de configuración e inyección de dependencias para todos los entornos del proyecto **Indra Notifications**, alineada con las mejores prácticas de Spring Boot y la metodología *The Twelve-Factor App*.

---

## 1. Jerarquía de Entornos y Nomenclatura

El proyecto establece una separación clara de perfiles mediante `application-{profile}.properties`, asegurando que el comportamiento de la aplicación sea predecible y reproducible:

| Entorno | Perfil Spring | Archivo de Configuración | Propósito | Implementación de `EmailSender` |
|---|---|---|---|---|
| **Local / Developer** | `dev` (default) | `application-dev.properties` | Desarrollo diario local sin dependencias de red externas. | `FakeEmailSender` (`@Profile("dev")`) |
| **Integración / QA** | `qa` | `application-qa.properties` | Pruebas funcionales y de regresión automatizadas. | `ConditionalEmailSender` (`@ConditionalOnProperty`) |
| **Staging / Pre-Prod** | `staging` | `application-staging.properties` | Pruebas de carga y validación pre-lanzamiento en infraestructura análoga a producción. | `SmtpEmailSender` (`@Profile("staging")`) |
| **Producción** | `prod` | `application-prod.properties` | Entorno productivo de alta disponibilidad con auditoría y reintentos estrictos. | `SmtpEmailSender` (`@Profile("prod")`) |
| **Testing Automatizado** | N/A | `@TestConfiguration` | Pruebas unitarias y de integración continuas en pipelines de CI/CD. | `TestEmailSender` (`@Primary`) |

---

## 2. Principios de Diseño y Buenas Prácticas

### 2.1 Inversión de Control (IoC) e Inyección por Constructor
* **Regla estricta:** Cero `@Autowired` en atributos de clases.
* Todas las dependencias obligatorias se declaran `private final` y se inyectan a través del constructor. Esto garantiza:
  1. **Inmutabilidad** de las referencias del servicio.
  2. **Imposibilidad de instanciación con estado inconsistente** (falla en tiempo de compilación si falta una dependencia).
  3. **Facilidad de pruebas unitarias puras** (instanciación directa con mocks sin necesidad de levantar el contexto de Spring ni usar reflexión).

### 2.2 Exclusión Mutua de Beans por Perfil
* Los beans que implementan la misma interfaz (`EmailSender`) deben ser mutuamente excluyentes para evitar ambigüedades (`NoUniqueBeanDefinitionException`):
  * `FakeEmailSender` activo exclusivamente en `@Profile("dev")`.
  * `SmtpEmailSender` activo en `@Profile("prod")`.
* La lógica de negocio (`NotificationService`) **nunca evalúa variables de entorno** (`System.getenv` o condicionales `if (env == "prod")`). Spring IoC se encarga del wiring automático en el arranque.

### 2.3 Feature Toggles mediante `@ConditionalOnProperty`
* Para habilitar o alternar proveedores de forma dinámica sin recompilar, se adopta `@ConditionalOnProperty`:
  ```java
  @Component
  @Profile("qa")
  @ConditionalOnProperty(prefix = "notification.email", name = "provider", havingValue = "conditional")
  public class ConditionalEmailSender implements EmailSender { ... }
  ```
* Permite cambiar estrategias en tiempo de ejecución o despliegue mediante propiedades externas (`notification.email.provider=conditional`).

### 2.4 Gestión de Secretos y Configuración Externa (12-Factor App)
* **Cero secretos en el código fuente:** Ninguna contraseña SMTP, API Key o token se almacena en `application*.properties`.
* En entornos desplegados (`staging`, `prod`):
  * Las credenciales se inyectan como variables de entorno o mediante proveedores de secretos (Azure Key Vault, Spring Cloud Config).
  * Los archivos de propiedades solo contienen placeholders estructurados:
    ```properties
    spring.mail.host=${SMTP_HOST:smtp.indra.es}
    spring.mail.username=${SMTP_USERNAME}
    spring.mail.password=${SMTP_PASSWORD}
    ```

### 2.5 Aislamiento de Pruebas con `@TestConfiguration`
* Para pruebas de integración donde se requiere inspeccionar los mensajes enviados sin llamar a la red ni alterar los perfiles de la aplicación:
  * Se utiliza `@TestConfiguration` con un bean `@Primary` (`TestEmailSender`).
  * No contamina los contextos de `dev` ni `prod` y ofrece determinismo total en suites de regresión.
