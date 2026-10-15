# Barberia — Sistema de Gestion

Proyecto academico — Curso **Diseno de Sistemas de Informacion**, ITM Medellin
(grupo 580304012-10, docente Anibal Antonio Torres Canas).

Implementacion tecnica del **Entregable #2**, que consolida y materializa en
codigo el analisis, las reglas de negocio, el diagrama de clases y el
prototipo Figma del **Entregable #1**.

## Stack

- **Backend:** Java 17 + Spring Boot 3 (Web, Data JPA)
- **Frontend:** React 19 + Vite + React Router
- **Base de datos:** PostgreSQL

## Alcance del MVP implementado (Release 1 del Story Map, E1)

| Historia de Usuario (Jira) | Endpoint / Pantalla |
|---|---|
| HU-01 Agendar cita | `POST /api/citas` · pantalla Cliente |
| HU-02 Consultar disponibilidad | `GET /api/citas/barbero/{id}` |
| HU-05/06 Ver calendario del dia | pantalla Barbero |
| HU-07 Validar cruce de horario | `Barbero.tieneDisponibilidad()` (RN-04) |
| HU-08 Confirmacion de cita | patron Observer sobre `Cita` |
| HU-09/10 Listar barberos | `GET /api/barberos` |
| HU-12 Registrar corte realizado | `POST /api/cortes` |
| HU-13 Calcular comision automatica | patron Strategy (60/40 o 100/0 si es el dueno) |
| HU-14 Registrar gasto operativo | `POST /api/gastos` |
| HU-15/16 Ingresos y utilidad neta por periodo | `GET /api/reportes` (patron Builder) |
| HU-22/23/24 Iniciar sesion (3 roles) | `POST /api/auth/login` |

Solo se implementa el **flujo principal** de cada historia (sin manejo de
errores ni caminos alternos), segun el alcance definido para el prototipo
funcional del Entregable 2.

## Patrones de diseno GoF aplicados

1. **Strategy** — `CalculadoraComisionStrategy` (calculo de comision por corte).
2. **Builder** — `ReporteFinanciero.Builder` (ensamblado del reporte financiero).
3. **Observer** — `CitaSubject` / `ObservadorCita` (notificacion y actualizacion de agenda al cambiar el estado de una cita).

Ver la plantilla de analisis y seleccion de patrones GoF en `/docs` para el
detalle de la justificacion, alternativas consideradas y no solapamiento.

## Como ejecutar

### Backend

```bash
cd backend
mvn spring-boot:run
```

Requiere una base de datos PostgreSQL llamada `barberia` (ver
`src/main/resources/application.properties`). Al iniciar, `DataSeeder` crea
datos demo (1 dueno, 2 barberos, 1 cliente — clave `1234` para todos).

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Copia `.env.example` a `.env` si el backend corre en otra URL.

## Estructura del repositorio

```
backend/   Spring Boot (dominio, patrones GoF, API REST)
frontend/  React (pantallas del flujo principal del MVP)
docs/      Entregable 2 completo: documento final (docx/pdf), diagramas C4,
           diagramas de actividad, capturas del prototipo y plantillas
           GoF/SOLID diligenciadas
```

## Entregable 2 — documento final

`docs/Entregable_2_Barberia.docx` (y su version en PDF) contiene las 7
secciones solicitadas: portada, tabla de refinamiento E1->E2, arquitectura
C4 (4 niveles, con imagen + enlace editable + explicacion), patrones GoF,
prototipo funcional (capturas del flujo principal), diagramas de actividad
del MVP y analisis SOLID. Las plantillas GoF y SOLID diligenciadas estan en
`docs/Plantilla_GoF_Barberia.docx` y `docs/Plantilla_SOLID_Barberia.docx`.
