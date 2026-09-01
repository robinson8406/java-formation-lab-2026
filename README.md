# java-formation-lab-2026

![Java 21](https://img.shields.io/badge/Java-21-007396?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot)
![Azure DevOps](https://img.shields.io/badge/Azure%20DevOps-CI%2FCD-0078D7?logo=azuredevops)
![Sesiones](https://img.shields.io/badge/Sesiones-18-blueviolet)

Repositorio oficial del **Plan de Formación Java 2026** — Indra Colombia (~200 personas).  
Basado en los resultados del QC 2026 COL y CA&C. Una sesión semanal, cada jueves, 1 hora.

---

## ¿Por qué este modelo?

**Un único reto intermedio para todos los seniorities.**  
El nivel se evidencia en _cómo_ resolviste el problema — no en _qué_ problema resolviste.

- Sesiones **autocontenidas**: quien no asistió puede resolver el reto con `week-XX-start` + solución publicada.
- Cada reto incluye: **1 cambio funcional + 1 prueba + 1 criterio no funcional** (calidad / performance / operación / seguridad).
- Rama `week-XX-start` disponible antes del jueves; `week-XX-solution` publicada al cierre de sesión.

### Formato de sesión (1 hora)

| Segmento | Tiempo | Contenido |
|----------|--------|-----------|
| Contexto + demo | 0–10' | Qué vamos a mejorar y por qué |
| Reto intermedio único | 10–45' | Implementación real en el repo |
| Validación | 45–55' | Tests + pipeline verde |
| Cierre | 55–60' | Solución de referencia publicada |

---

## Calendario 2026 (jueves, ago–dic)

| # | Fecha | Paquete | Sesión | Estado |
|---|-------|---------|--------|--------|
| 01 | 2026-08-20 | Arranque | Kickoff + setup Java 21 | ✅ |
| 02 | 2026-08-27 | SOLID + TDD | TDD con JUnit 5 | ⏳ |
| 03 | 2026-09-03 | SOLID + TDD | SOLID (SRP/OCP) | ⏳ |
| 04 | 2026-09-10 | SOLID + TDD | Patrones (Strategy/Factory) | ⏳ |
| 05 | 2026-09-17 | SOLID + TDD | Mockito (buenas prácticas) | ⏳ |
| 06 | 2026-09-24 | Spring Web / Testing | REST limpio (DTO/errores) | ⏳ |
| 07 | 2026-10-01 | Spring Web / Testing | DI/IoC (profiles/wiring) | ⏳ |
| 08 | 2026-10-08 | Spring Web / Testing | WebMvcTest (slice tests) | ⏳ |
| 09 | 2026-10-15 | Hibernate / JPA | Mapeos JPA | ⏳ |
| 10 | 2026-10-22 | Hibernate / JPA | Transacciones | ⏳ |
| 11 | 2026-10-29 | Hibernate / JPA | Performance ORM | ⏳ |
| 12 | 2026-11-05 | Hibernate / JPA | JPQL + paginación + locking | ⏳ |
| 13 | 2026-11-12 | Micro + Cloud + DevOps | Microservicios | ⏳ |
| 14 | 2026-11-19 | Micro + Cloud + DevOps | Podman + compose | ⏳ |
| 15 | 2026-11-26 | Micro + Cloud + DevOps | Azure DevOps Pipelines (CI) | ⏳ |
| 16 | 2026-12-03 | Micro + Cloud + DevOps | Azure DevOps Release | ⏳ |
| 17 | 2026-12-10 | Micro + Cloud + DevOps | Azure App Service | ⏳ |
| 18 | 2026-12-17 | Micro + Cloud + DevOps | Observabilidad + cierre | ⏳ |

**Vista por mes**

| Mes | Semanas | Paquete |
|-----|---------|--------|
| Agosto | 2 sesiones (sem 1–2) | Arranque + SOLID/TDD (inicio) |
| Septiembre | 4 sesiones (sem 3–6) | SOLID+TDD (cierre) + Spring Web (inicio) |
| Octubre | 5 sesiones (sem 7–11) | Spring Web (cierre) + Hibernate |
| Noviembre | 4 sesiones (sem 12–15) | Hibernate (cierre) + Micro/Cloud/DevOps (inicio) |
| Diciembre | 3 sesiones (sem 16–18) | Micro/Cloud/DevOps (cierre) |

<details>
<summary>Sesiones descartadas del plan (fortalezas o bajo impacto QC)</summary>

- Git + PR hygiene → Git es fortaleza
- Maven multi-módulo → Maven es fortaleza
- DataJpaTest → cubierto dentro del paquete Hibernate
- Spring Security → no aparece en las brechas QC prioritarias
- OpenAPI → se cubre dentro de Microservicios
- Projections/read models → menor prioridad en Hibernate/JPA
</details>

---

## ¿Cómo empiezo?

1. **Lee** [REGLAS_DE_JUEGO.md](REGLAS_DE_JUEGO.md) y [docs/onboarding-java21.md](docs/onboarding-java21.md).
2. **Haz fork** del repo en GitHub y clona **tu fork**:
   ```bash
   git clone https://github.com/TU-ALIAS/java-formation-lab-2026.git
   git remote add upstream https://github.com/robinson8406/java-formation-lab-2026.git
   ```
3. **Crea tu rama** para cada sesión a partir del start del formador:
   ```bash
   git fetch upstream
   git checkout -b week-01/tu-alias upstream/week-01-start
   ```
4. **Resuelve el reto**, haz push a tu fork y abre un PR hacia `week-01-start` en el repo central. Los PRs **no se mergean** — solo sirven para CI + peer review + badge.

---

## Estructura del repositorio

```
java-formation-lab-2026/
├── README.md
├── REGLAS_DE_JUEGO.md
├── .github/
│   ├── PULL_REQUEST_TEMPLATE.md
│   ├── ISSUE_TEMPLATE/badge-completado.md
│   └── labels.yml
├── docs/
│   └── onboarding-java21.md
└── sessions/
    └── week-XX-<tema>/
        ├── README.md
        ├── enunciado.md
        ├── criterios-evaluacion.md
        └── solucion-referencia/
```

---

## Stack

`Java 21` · `Spring Boot 3.x` · `Hibernate 6` · `JUnit 5` · `Mockito` · `Azure DevOps` · `Podman` · `Azure App Service`