# Imagen de un solo servicio para la nube: el backend sirve también la aplicación web.
# Con Docker Compose se usan en cambio los Dockerfile de backend/ y frontend/.

# --- Etapa 1: aplicación web -----------------------------------------------
FROM node:20-alpine AS frontend
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# --- Etapa 2: backend con la aplicación web en classpath:/static -----------
FROM maven:3.9.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY backend/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /frontend/dist ./src/main/resources/static
# Las pruebas se corren antes de publicar (mvnw test y npm test); aquí solo se empaqueta.
RUN mvn -B -q -DskipTests package

# --- Etapa 3: ejecución ----------------------------------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system barberia && useradd --system --gid barberia barberia
COPY --from=backend /app/target/barberia-backend-*.jar app.jar
USER barberia

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
