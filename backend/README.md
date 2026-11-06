# Backend – Sistema de gestión de barbería (MVP)

Java 21 · Spring Boot · PostgreSQL · Maven

## Conexión a la base de datos
La base de datos se crea por separado. El backend se conecta con estas variables
(valores por defecto en `application.yml`):

- `DB_URL` (por defecto `jdbc:postgresql://localhost:5432/barberia`)
- `DB_USER`
- `DB_PASSWORD`

## Ejecución
```bash
mvn spring-boot:run
```

## Pruebas
```bash
mvn test
```

## Estructura
```
com.barberia
├── dominio       entidades del nivel 4 (sin dependencias hacia otras capas)
└── repositorio   acceso a datos (Spring Data JPA)
```
