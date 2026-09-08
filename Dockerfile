# Multi-stage build: frontend (Vite) -> backend (Gradle/Kotlin) -> slim runtime.
# Build from the repo root, e.g.:
#   docker compose -f docker-compose.prod.yml build

# --- Stage 1: build the frontend ---
FROM node:22-slim AS frontend-build
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# --- Stage 2: build the backend (bundles the frontend build as classpath resources) ---
FROM eclipse-temurin:21-jdk-jammy AS backend-build
WORKDIR /app

# Copy just the files Gradle needs to resolve dependencies first, so that step
# stays cached across rebuilds that only change application source.
COPY gradlew settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/
COPY backend/build.gradle.kts backend/build.gradle.kts
RUN ./gradlew :backend:dependencies --no-daemon

COPY backend/ backend/
COPY --from=frontend-build /frontend/dist/ backend/src/main/resources/public/

# Requires `application { mainClass.set("uk.derbyshire.MainKt") }` in
# backend/build.gradle.kts — see note below.
RUN ./gradlew :backend:installDist --no-daemon

# --- Stage 3: slim runtime image ---
FROM eclipse-temurin:21-jre-jammy AS runtime

RUN useradd --system --create-home --shell /usr/sbin/nologin app
WORKDIR /app
COPY --from=backend-build /app/backend/build/install/backend/ ./
RUN chown -R app:app /app
USER app

EXPOSE 8080
ENTRYPOINT ["/app/bin/backend"]
