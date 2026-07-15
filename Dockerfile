# Stage 1: Build
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /opt/app

# Kopiujemy pliki gradle (Kotlin DSL)
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./

# Kopiujemy moduły
COPY backend/ backend/
COPY frontend/ frontend/

# Budujemy aplikację (backend automatycznie zbuduje frontend)
RUN chmod +x ./gradlew
RUN ./gradlew :backend:bootJar -x test -x integrationTest --no-daemon

# Stage 2: Run
FROM eclipse-temurin:25-jre
WORKDIR /opt/app

# Curl do healthchecka
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /opt/app/backend/build/libs/*.jar /opt/app/app.jar
EXPOSE 8082

HEALTHCHECK --interval=5s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /opt/app/app.jar"]
