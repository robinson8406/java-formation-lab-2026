# Criterios de evaluación — Week 14

## Checklist del reviewer

### Dockerfile
- [ ] Multi-stage: stage `builder` con JDK, stage `runtime` con JRE
- [ ] Imagen final < 300MB (verificar con `podman images`)
- [ ] Usuario no-root en el stage runtime
- [ ] Sin secretos ni credenciales hardcodeadas

### Podman Compose
- [ ] Servicios `app` y `db` definidos en `compose.yml`
- [ ] Health check en `db` con `pg_isready`
- [ ] `app` depende de `db` con `condition: service_healthy`
- [ ] Volumen persistente para datos de Postgres
- [ ] Red interna `app-network`

### Variables de entorno
- [ ] `.env.example` con todas las variables requeridas
- [ ] `.env` en `.gitignore`
- [ ] App lee config desde variables de entorno (no hardcodeada en properties)

### Verificación
- [ ] `podman compose up --build` funciona sin errores
- [ ] `GET http://localhost:8080/actuator/health` retorna 200
- [ ] `mvn verify` en verde

## Escala de madurez

| Junior | Semi-senior | Senior | Experto |
|--------|-------------|--------|---------|
| Dockerfile funcional, compose básico | Multi-stage, usuario no-root, health check | JAVA_OPTS, .env, imagen < 200MB | Propone estrategia de CI con Podman en modo rootless, multi-platform, y cache de layers |
