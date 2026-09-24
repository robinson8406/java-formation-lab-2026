# Guía de Preparación — Week 01: Kickoff + Setup Java 21

> **Audiencia**: Formadores y mentores  
> **Fecha sesión**: 2026-08-20 (jueves)  
> **Tiempo de preparación estimado**: ~2 horas  

---

## RESUMEN DE LO QUE NECESITAS DEJAR LISTO

| # | Qué | Dónde | Cuándo |
|---|-----|-------|--------|
| 1 | Repo público en GitHub con rama `week-01-start` | GitHub | D-7 (antes del 13 ago) |
| 2 | Esqueleto de código en `week-01-start` (TODOs) | GitHub | D-7 |
| 3 | Pipeline conectado en Azure DevOps | Azure DevOps | D-5 (antes del 15 ago) |
| 4 | PR trigger validado (test rojo → pipeline falla) | Azure DevOps | D-3 |
| 5 | Status check obligatorio en `week-01-start` | GitHub → Settings | D-3 |
| 6 | Comunicar a participantes: fork + prereqs | Canal de comunicación | D-2 |
| 7 | Rama `week-01-solution` preparada (no publicar aún) | Git local | D-1 |

---

## PASO 1 — GitHub: Preparar el repositorio central

### 1.1 Verificar que el repo es público

```
https://github.com/robinson8406/java-formation-lab-2026
Settings → General → Danger Zone → Change visibility → Public
```

> ⚠️ Debe ser público para que los participantes hagan fork sin costo.

---

### 1.2 Crear y publicar la rama `week-01-start`

```bash
# Desde tu máquina local (repo central clonado)
git checkout main
git pull origin main

# Crea la rama del skeleton
git checkout -b week-01-start

# El código ya está en sessions/week-01-kickoff/
# Verifica que TrackingIdGenerator.java tiene el TODO (NO la solución)
cat sessions/week-01-kickoff/src/main/java/com/indra/logistics/TrackingIdGenerator.java
# Debe mostrar: throw new UnsupportedOperationException("Not implemented yet");

# Verifica que los tests tienen fail("Test no implementado")
cat sessions/week-01-kickoff/src/test/java/com/indra/logistics/TrackingIdGeneratorTest.java

# Publica la rama
git push origin week-01-start
```

> ✅ Los participantes harán `git fetch upstream` y verán `week-01-start` disponible.

---

### 1.3 Configurar protección de rama en GitHub

```
GitHub → Settings → Branches → Add branch protection rule
Branch name pattern: week-*-start

Marcar:
  ✅ Require a pull request before merging
  ✅ Require status checks to pass before merging
     → buscar: "Build & Test — week-01-kickoff"  (se crea en paso 3)
  ✅ Do not allow bypassing the above settings
  ❌ Restrict pushes (NO: los participantes no van a pushear directo)
```

> Esto garantiza que el CI corra en cada PR y bloquea el merge sin pipeline verde.

---

### 1.4 Verificar labels en el repo

```
GitHub → Labels
```

Deben existir estos labels (ya están en `.github/labels.yml`):

| Label | Color | Uso |
|-------|-------|-----|
| `✅ completado` | verde | Badge de sesión completada |
| `week-01` | azul | Filtra por sesión |
| `needs-review` | amarillo | PR esperando reviewer |

Para crear/importar los labels desde el archivo:

```bash
# Instalar gh CLI si no lo tienes: https://cli.github.com
gh auth login
gh label create "✅ completado" --color "0e8a16" --description "Sesión completada y PR aprobado"
gh label create "week-01" --color "0075ca" --description "Week 01 - Kickoff"
gh label create "needs-review" --color "e4e669" --description "Esperando peer review"
```

---

## PASO 2 — Azure DevOps: Conectar el pipeline

### 2.1 Crear la organización y proyecto (si no existe)

```
1. Ir a https://dev.azure.com
2. Crear organización: indra-java-2026  (o la que ya tengas)
3. Crear proyecto: java-formation-lab-2026
   Visibility: Private (los pipelines son internos)
```

---

### 2.2 Conectar GitHub como Service Connection

```
Azure DevOps → Project Settings → Service connections
→ New service connection → GitHub → OAuth o PAT

Nombre de la conexión: github-java-formation
Repo autorizado: robinson8406/java-formation-lab-2026
```

> ⚠️ Si usas PAT (Personal Access Token de GitHub), necesita permisos:
> `repo`, `read:org`, `admin:repo_hook`

---

### 2.3 Crear el pipeline desde el archivo YAML

```
Azure DevOps → Pipelines → New pipeline
→ GitHub → robinson8406/java-formation-lab-2026
→ Existing Azure Pipelines YAML file
→ Branch: week-01-start
→ Path: sessions/week-01-kickoff/azure-pipelines.yml
→ Save (NO ejecutar aún)

Nombre del pipeline: week-01-kickoff-ci
```

El archivo `azure-pipelines.yml` ya está configurado para:
- Activarse solo en PRs hacia `week-*-start`
- Ejecutar `mvn verify` con JDK 21
- Publicar resultados JUnit
- Reportar cobertura JaCoCo

---

### 2.4 Validar que el pipeline corre correctamente

```bash
# Crea un PR de prueba desde tu máquina
git checkout -b week-01/test-formador upstream/week-01-start
# Haz un cambio mínimo (ej: agrega un comentario en el test)
git commit -m "test: validar pipeline CI"
git push origin week-01/test-formador

# Abre PR en GitHub:
# tu-alias:week-01/test-formador → robinson8406:week-01-start
```

Esperado en Azure DevOps:
```
✅ Pipeline se activa automáticamente
❌ mvn verify FALLA (porque los tests tienen fail("Test no implementado"))
✅ El estado se reporta en el PR de GitHub como check fallido → correcto
```

> ✅ Este comportamiento es el CORRECTO: el skeleton falla en rojo. El participante debe hacerlo verde.

Borra el PR de prueba después.

---

### 2.5 Configurar el status check en GitHub (enlazar ADO → GitHub)

```
Azure DevOps → Pipeline → ... → Triggers → 
  Enable "Pull request validation"
  Comment trigger: /azp run (para re-ejecutar manualmente)

GitHub → Settings → Branches → week-*-start (protection rule)
→ Status checks → buscar el nombre del pipeline de ADO
→ Marcarlo como requerido
```

---

## PASO 3 — Preparar la solución de referencia (NO publicar aún)

La solución se publica en el minuto 55 de la sesión. Prepárala en local:

```bash
git checkout -b week-01-solution upstream/week-01-start
```

Implementa `TrackingIdGenerator.java`:

```java
package com.indra.logistics;

import java.security.SecureRandom;

public class TrackingIdGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_PART_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    public String generate(String origin, String destination) {
        validateNotBlank(origin, "origin");
        validateNotBlank(destination, "destination");
        return origin.toUpperCase() + "-" + destination.toUpperCase() + "-" + randomAlphanumeric();
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
    }

    private String randomAlphanumeric() {
        StringBuilder sb = new StringBuilder(RANDOM_PART_LENGTH);
        for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
```

Implementa los tests en `TrackingIdGeneratorTest.java`:

```java
package com.indra.logistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @Test
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat() {
        String id = generator.generate("BOG", "MED");

        assertTrue(id.matches("BOG-MED-[A-Z0-9]{8}"),
            "Formato esperado: BOG-MED-XXXXXXXX, recibido: " + id);
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin es nulo")
    void shouldThrowWhenOriginIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> generator.generate(null, "MED"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si destination es vacío")
    void shouldThrowWhenDestinationIsBlank() {
        assertThrows(IllegalArgumentException.class,
            () -> generator.generate("BOG", ""));
    }

    @Test
    @DisplayName("Dos IDs generados para el mismo origen/destino deben ser distintos")
    void shouldGenerateUniqueIds() {
        String id1 = generator.generate("BOG", "MED");
        String id2 = generator.generate("BOG", "MED");
        assertNotEquals(id1, id2, "Los IDs deben ser únicos");
    }

    // BONUS: test parametrizado con @ParameterizedTest
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe lanzar excepción para cualquier valor inválido de origin")
    void shouldThrowForInvalidOrigin(String invalidOrigin) {
        assertThrows(IllegalArgumentException.class,
            () -> generator.generate(invalidOrigin, "MED"));
    }
}
```

```bash
# Verifica que la solución pasa localmente
cd sessions/week-01-kickoff
./mvnw verify
# Esperado: BUILD SUCCESS, todos los tests verdes

# Guarda en local, NO hagas push todavía
git add .
git commit -m "feat: implementación de referencia TrackingIdGenerator"
# NO: git push origin week-01-solution  ← publicar solo en minuto 55
```

---

## PASO 4 — Comunicar a los participantes (D-2, martes 18 ago)

Mensaje para canal Teams / Slack / email:

```
📣 Semana 01 — Kickoff Java 2026

Antes del jueves necesitas:

1. ✅ Java 21 instalado → java -version
2. ✅ Cuenta GitHub (gratuita)
3. ✅ IDE (IntelliJ IDEA Community recomendado)
4. ✅ Hacer FORK del repo central:
   https://github.com/robinson8406/java-formation-lab-2026
   → clic en botón "Fork" → crear en tu cuenta

5. Clonar TU fork (no el central):
   git clone https://github.com/TU-ALIAS/java-formation-lab-2026.git
   cd java-formation-lab-2026
   git remote add upstream https://github.com/robinson8406/java-formation-lab-2026.git

6. Verificar que el build pasa en main:
   cd sessions/week-01-kickoff
   ./mvnw verify
   → esperado: BUILD FAILURE (los tests tienen fail() — eso está bien)
   → esperado: COMPILA sin errores de compilación

Nos vemos el jueves 20 ago, 1h.
Repo: https://github.com/robinson8406/java-formation-lab-2026
```

---

## PASO 5 — Flujo de la sesión (script para formador)

### 0–10' — Contexto + Demo del repo

```
Mostrar en pantalla:
1. GitHub: estructura del repo → sessions/week-01-kickoff/
2. Qué es una rama week-XX-start vs week-XX-solution
3. Diagrama de fork (slides: presentacion-participantes.html → slide "¿Qué es un fork?")
4. Mostrar el archivo enunciado.md en vivo
5. Abrir TrackingIdGenerator.java → "esto es lo que van a implementar"
```

### 10–45' — Reto en vivo

```
Participantes ejecutan:

git fetch upstream
git checkout -b week-01/su-alias upstream/week-01-start
cd sessions/week-01-kickoff
./mvnw verify   ← falla en rojo: normal, ese es el punto de partida
```

**Como formador, acompaña mostrando:**

```bash
# Mostrar en pantalla — flujo de trabajo
git fetch upstream
git checkout -b week-01/demo-formador upstream/week-01-start

# Abrir TrackingIdGenerator.java en el IDE
# Mostrar el TODO → "implementen esto"
# Abrir TrackingIdGeneratorTest.java → "estos tests deben pasar en verde"
```

### 45–55' — Validación

```
Participante que terminó:
  1. ./mvnw verify → BUILD SUCCESS
  2. git push origin week-01/su-alias
  3. Abrir PR en GitHub:
     TU-ALIAS:week-01/su-alias → robinson8406:week-01-start
  4. Mostrar en pantalla: Azure DevOps corre el pipeline automáticamente
  5. Esperar ✅ en el PR de GitHub
```

**Mostrar en Azure DevOps:**
```
Pipelines → week-01-kickoff-ci → último run
→ logs de mvn verify
→ Test Results: X tests, 0 failures
→ Code Coverage: XX%
```

### 55–60' — Cierre y solución de referencia

```bash
# AHORA sí: publicar la solución
git push origin week-01-solution
```

```
GitHub → ramas → week-01-solution disponible
Mostrar la implementación de referencia en pantalla
Destacar: SecureRandom (criterio de seguridad), Record de Java 21 (bonus)
```

**Badge flow (mostrar en vivo):**
```
GitHub → Issues → New Issue
Usar template: .github/ISSUE_TEMPLATE/badge-completado.md
Título: ✅ [week-01] su-alias completó la sesión
Asignar label: ✅ completado
Cerrar el issue
→ participante tiene su badge
```

---

## PASO 6 — Verificación post-sesión (checklist formador)

- [ ] Rama `week-01-solution` publicada
- [ ] Al menos 1 issue de badge creado y cerrado (como demo)
- [ ] Próxima rama `week-02-start` preparada (publicar lunes 24 ago)
- [ ] Registrar en canal: % de participantes con PR verde

---

## TROUBLESHOOTING COMÚN

| Problema | Causa probable | Solución |
|----------|---------------|----------|
| `./mvnw: Permission denied` | Sin permiso de ejecución | `chmod +x mvnw` |
| `java: command not found` | JAVA_HOME no configurado | Ver `docs/onboarding-java21.md` |
| Pipeline no se activa | PR no apunta a `week-*-start` | Verificar rama destino del PR |
| `upstream` no encontrado | No agregaron el remote | `git remote add upstream https://github.com/robinson8406/java-formation-lab-2026.git` |
| Fork no ve `week-01-start` | No hicieron `git fetch upstream` | `git fetch upstream` antes del checkout |
| PR cross-fork no dispara CI | Service connection ADO sin permisos | Revisar permisos del PAT en Azure DevOps |

---

## LINKS RÁPIDOS

| Recurso | URL |
|---------|-----|
| Repo central | https://github.com/robinson8406/java-formation-lab-2026 |
| Azure DevOps | https://dev.azure.com/indra-java-2026 |
| Onboarding Java 21 | `docs/onboarding-java21.md` |
| Reglas de juego | `REGLAS_DE_JUEGO.md` |
| Slides participantes | `docs/presentacion-participantes.html` |
| Slides formadores | `docs/presentacion-formadores.html` |
