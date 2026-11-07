# Backend – Sistema de gestión de barbería (MVP)

Java 17 · Spring Boot · PostgreSQL · Maven (incluido con el wrapper `mvnw`)

## Conexión a la base de datos
La base de datos se crea por separado. El backend se conecta con estas variables
(valores por defecto en `application.yml`):

- `DB_URL` (por defecto `jdbc:postgresql://localhost:5432/barberia`)
- `DB_USER`
- `DB_PASSWORD`

## Seguridad
El backend exige la variable `JWT_SECRETO` (mínimo 32 caracteres) para firmar los tokens.
Nunca se guarda en el repositorio. Ejemplo en PowerShell:

```powershell
$env:JWT_SECRETO = "una-frase-larga-y-secreta-de-al-menos-32-caracteres"
```

Cada solicitud, salvo registro e inicio de sesión, lleva la cabecera
`Authorization: Bearer <token>`. El token dura 8 horas (`barberia.seguridad.duracion-token`).

| Rol | Acceso |
| --- | --- |
| CLIENTE | agendar, ver disponibilidad y sus propias citas |
| BARBERO | su calendario, atender sus citas, registrar cortes y ver sus comisiones |
| DUENO | todo lo del barbero, calendario general, gastos y reportes |

## Horario de atención
Las franjas que se ofrecen al agendar salen de `barberia.horario` en `application.yml`
(apertura, cierre, duración del turno y zona horaria).

## Ejecución
```powershell
.\mvnw.cmd spring-boot:run   # Windows
./mvnw spring-boot:run       # Linux o macOS
```

## Pruebas
```powershell
.\mvnw.cmd test   # Windows
./mvnw test       # Linux o macOS
```
La primera vez, el wrapper descarga Maven; no hace falta instalarlo.

## API de autenticación
| Método | Ruta | Historia |
| --- | --- | --- |
| POST | `/api/auth/registro` | HU-21 |
| POST | `/api/auth/login` | HU-22, HU-23, HU-24 |
| GET  | `/api/auth/yo` | usuario autenticado |

## API de agendamiento
| Método | Ruta | Historia |
| --- | --- | --- |
| GET  | `/api/barberos` | HU-01 (elegir barbero) |
| GET  | `/api/barberos/{id}/disponibilidad?fecha=AAAA-MM-DD` | HU-02 |
| POST | `/api/citas` | HU-01, HU-07 |
| GET  | `/api/citas/mias` | HU-24 |
| GET  | `/api/barberos/{id}/citas?fecha=AAAA-MM-DD` | HU-05 |
| GET  | `/api/citas?fecha=AAAA-MM-DD` | HU-06 |

## API de atención
| Método | Ruta | Historia |
| --- | --- | --- |
| PATCH | `/api/citas/{id}/atendida` | HU-12 |
| POST  | `/api/citas/{id}/corte` | HU-27, HU-13 |
| POST  | `/api/cortes` | HU-28, HU-13 |
| GET   | `/api/cortes/mios?desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | HU-23, HU-26 |

## API de gastos y reportes
| Método | Ruta | Historia |
| --- | --- | --- |
| POST | `/api/gastos` | HU-14 |
| GET  | `/api/reportes/ingresos?desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | HU-15 |
| GET  | `/api/reportes/gastos?desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | HU-29 |
| GET  | `/api/reportes/utilidad-neta?desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | HU-16 |

## Estructura
```
com.barberia
├── dominio       entidades del nivel 4 (sin dependencias hacia otras capas)
├── repositorio   acceso a datos (Spring Data JPA)
├── politica      políticas de comisión (Strategy)
├── reporte       reportes financieros por periodo (Template Method)
├── servicio      casos de uso
│   ├── agenda    agendamiento y consultas de agenda
│   ├── atencion  flujo de atención (Facade) y consulta de cortes
│   ├── autenticacion  registro e inicio de sesión
│   └── gasto     registro de gastos
├── seguridad     tokens JWT y reglas de acceso por rol (HU-26)
├── web           controladores REST y manejo de errores
└── config        configuración (reloj)
```
