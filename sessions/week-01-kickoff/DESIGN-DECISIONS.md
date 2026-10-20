# Decisiones de Diseño y Arquitectura — TrackingIdGenerator (Nivel Experto)

> Documento de justificación técnica y análisis de ingeniería para el generador de identificadores de seguimiento de **Indra Logistics** (Semana 01: Kickoff + Setup Java 21).

---

## 1. Value Object Pattern con Records de Java 21

Se implementó el record [`Route`](file:///C:/Users/dfmunoz/Documents/java-formation-lab-2026/sessions/week-01-kickoff/src/main/java/com/indra/logistics/Route.java) como un **Value Object** inmutable:
* **Constructor Compacto**: Centraliza la validación de invariantes de negocio en el momento de la instanciación.
* **Normalización Automática**: Realiza `trim()` y conversión a mayúsculas (`toUpperCase()`), garantizando que la representación interna siempre sea canónica (e.g. `" bog "` se normaliza a `"BOG"`).
* **Inmutabilidad por Diseño**: Al ser un `record`, los componentes son `private final`, con implementaciones automáticas y consistentes de `equals()`, `hashCode()` y `toString()`.

---

## 2. Generación Criptográfica y Análisis de Colisiones

### ¿Por qué `SecureRandom` y no `java.util.Random` o `Math.random()`?
1. **Seguridad e Impredecibilidad**: `java.util.Random` se basa en un generador congruencial lineal (LCG) determinista con una semilla de 48 bits. Un atacante que observe un par de IDs consecutivos puede calcular la semilla interna y predecir los siguientes tracking IDs emitidos, permitiendo ataques de enumeración o interceptación de pedidos.
2. **CSPRNG**: `SecureRandom` aprovecha las fuentes de entropía del sistema operativo (e.g. `/dev/urandom` en Linux o `CryptoAPI` en Windows), garantizando una distribución estadística uniforme y criptográficamente segura.
3. **Ausencia de Números Mágicos**: Constantes explícitas como `RANDOM_PART_LENGTH = 8` y `ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"`.

### Análisis de Espacio de Estados y Paradoja del Cumpleaños:
* El alfabeto contiene $36$ símbolos posibles ($26$ letras mayúsculas $+ 10$ dígitos).
* Con una longitud de $8$ caracteres:
  $$\text{Combinaciones} = 36^8 = 2,821,109,907,456 \approx 2.82 \times 10^{12}$$
* Para cada ruta específica (por ejemplo, `BOG-MED`), existen más de **2.8 billones** de códigos posibles.
* Según la aproximación de la paradoja del cumpleaños ($p \approx 1 - e^{-n^2 / (2N)}$), se requerirían aproximadamente **$2.37$ millones de envíos** en la misma ruta antes de que la probabilidad de colisión alcance el $0.1\%$.
* La suite incluye una prueba de verificación de unicidad con $1,000$ IDs consecutivos para garantizar empíricamente la ausencia de colisiones en ráfagas de tráfico.

---

## 3. Matriz de Edge Cases Mitigados

| Edge Case | Comportamiento Esperado | Manejo en el Código |
|---|---|---|
| Parámetro `null` | Rechazo inmediato con mensaje explicativo | Lanza `IllegalArgumentException("El origen/destino de la ruta no puede ser nulo...")` |
| Cadena vacía `""` | Rechazo inmediato | Detectado mediante `origin.isBlank()` $\rightarrow$ `IllegalArgumentException` |
| Espacios en blanco `"   \t\n"` | Rechazo inmediato | Detectado mediante `origin.isBlank()` $\rightarrow$ `IllegalArgumentException` |
| Minúsculas y espacios marginales | Limpieza y normalización canónica | `origin.trim().toUpperCase()` produce formato estándar `ORIG-DEST-XXXXXXXX` |
| Objeto `Route` nulo | Detección de puntero nulo temprana | `Objects.requireNonNull(route, "La ruta no puede ser nula")` $\rightarrow$ `NullPointerException` |
| Coexistencia API tradicional y Value Object | Compatibilidad hacia atrás y diseño moderno | Sobrecarga de `generate(String, String)` delegando en `generate(Route)` |

---

## 4. Estrategia de Testing (21 tests ejecutados)
1. **Formato**: Expresión regular canónica `^BOG-MED-[A-Z0-9]{8}$`.
2. **Excepciones específicas**: Uso de `assertThrows(IllegalArgumentException.class)` y `assertThrows(NullPointerException.class)` verificando los mensajes exactos de error (evitando capturar `Exception.class` genérico).
3. **Pruebas Parametrizadas**:
   - `@ParameterizedTest` con `@NullAndEmptySource` y `@ValueSource` para combinaciones de espacios en blanco.
   - `@ParameterizedTest` con `@CsvSource` para múltiples rutas aeroportuarias (`BOG-MED`, `CLO-BAQ`, `MAD-BCN`, `JFK-LHR`).
4. **Verificación de unicidad**: Test determinista que acumula $1,000$ IDs en un `Set` asegurando $0$ colisiones.
