# Stage 1: compilación
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

# Stage 2: ejecución
FROM bellsoft/liberica-runtime-container:jre-21-slim-musl AS runtime

WORKDIR /app

RUN addgroup -S appgroup \
    && adduser -S -G appgroup appuser

USER appuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:-} -jar app.jar"]