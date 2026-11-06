# Backend – Sistema de gestión de barbería (MVP)

Java 21 · Spring Boot · PostgreSQL · Maven

## Conexión a la base de datos
La base de datos se crea por separado. El backend se conecta con estas variables
(valores por defecto en `application.yml`):

- `DB_URL` (por defecto `jdbc:postgresql://localhost:5432/barberia`)
- `DB_USER`
- `DB_PASSWORD`

## Horario de atención
Las franjas que se ofrecen al agendar salen de `barberia.horario` en `application.yml`
(apertura, cierre, duración del turno y zona horaria).

## Ejecución
```bash
mvn spring-boot:run
```

## Pruebas
```bash
mvn test
```

## API de agendamiento
| Método | Ruta | Historia |
| --- | --- | --- |
| GET  | `/api/barberos` | HU-01 (elegir barbero) |
| GET  | `/api/barberos/{id}/disponibilidad?fecha=AAAA-MM-DD` | HU-02 |
| POST | `/api/citas` | HU-01, HU-07 |
| GET  | `/api/barberos/{id}/citas?fecha=AAAA-MM-DD` | HU-05 |
| GET  | `/api/citas?fecha=AAAA-MM-DD` | HU-06 |

## API de atención
| Método | Ruta | Historia |
| --- | --- | --- |
| PATCH | `/api/citas/{id}/atendida` | HU-12 |
| POST  | `/api/citas/{id}/corte` | HU-27, HU-13 |
| POST  | `/api/cortes` | HU-28, HU-13 |

## Estructura
```
com.barberia
├── dominio       entidades del nivel 4 (sin dependencias hacia otras capas)
├── repositorio   acceso a datos (Spring Data JPA)
├── politica      políticas de comisión (Strategy)
├── servicio      casos de uso
│   ├── agenda    agendamiento y consultas de agenda
│   └── atencion  flujo de atención (Facade)
├── web           controladores REST y manejo de errores
└── config        configuración (reloj)
```
