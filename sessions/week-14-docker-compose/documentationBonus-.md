# Mejoras agregadas Bonus

## Adminer para administrar PostgreSQL

Se agregó **Adminer** para explorar la base de datos desde el navegador.

### Acceso

Levantar los servicios:

```bash
podman compose up -build o podman compose up -d
```

Abrirel adminer:

```text
http://localhost:8081
```

### Datos de conexión

```text
Sistema: PostgreSQL
Servidor: db
Usuario: retail
Contraseña: retail123
Base de datos: retaildb
```

## Límite de memoria para la JVM

Se configuró la variable `JAVA_OPTS` para controlar el consumo de memoria de la aplicación.

```yaml
environment:
  JAVA_OPTS: "-Xms256m -Xmx512m"
```

- `-Xms256m`: memoria inicial.
- `-Xmx512m`: memoria máxima.

---

## Verificación

Estado de la aplicación:

```bash
curl http://localhost:8080/actuator/health
```
