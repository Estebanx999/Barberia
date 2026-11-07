# Imagen del backend: se compila con Maven y se ejecuta solo con el JRE.

# --- Etapa 1: compilación ---------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-17 AS compilacion
WORKDIR /app

# Primero las dependencias: se reutilizan en caché mientras el pom no cambie.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Las pruebas se corren antes de construir la imagen (mvnw test); aquí solo se empaqueta.
RUN mvn -B -q -DskipTests package

# --- Etapa 2: ejecución -----------------------------------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app

# La aplicación no corre como root.
RUN groupadd --system barberia && useradd --system --gid barberia barberia
COPY --from=compilacion /app/target/barberia-backend-*.jar app.jar
USER barberia

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
