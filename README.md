# Barbería

Sistema de gestión de citas y control contable para una barbería (MVP, Release 1).

| Carpeta | Contenido |
| --- | --- |
| `backend` | API REST en Java 17 con Spring Boot (ver `backend/README.md`) |
| `frontend` | Aplicación web en React con TypeScript (ver `frontend/README.md`) |
| `database` | Esquema PostgreSQL y cuentas iniciales |
| `docker-compose.yml` | Despliegue completo con Docker |

## Despliegue con Docker Compose

Levanta tres contenedores: PostgreSQL (con el esquema y las cuentas iniciales), el backend y el
frontend servido por Nginx. Solo se necesita Docker Desktop.

```powershell
Copy-Item .env.example .env      # luego editen .env: DB_PASSWORD y JWT_SECRETO
docker compose up -d --build
```

La aplicación queda en `http://localhost:8080` (cambia con `PUERTO_WEB` en `.env`).
La base de datos y el backend no se publican fuera de Docker; solo el frontend.

| Acción | Comando |
| --- | --- |
| Ver el estado | `docker compose ps` |
| Ver los registros del backend | `docker compose logs -f backend` |
| Detener | `docker compose down` |
| Detener y borrar los datos | `docker compose down -v` |

Los scripts de `database/` solo se ejecutan la primera vez, cuando el volumen de datos está vacío.

## Levantar todo en desarrollo (Windows)

Requisitos: JDK 17+, Node.js 20+ y Docker Desktop abierto. Maven no hace falta: el backend usa
su wrapper (`mvnw.cmd`).

```powershell
powershell -ExecutionPolicy Bypass -File .\levantar-dev.ps1
```

El script:

1. revisa las herramientas;
2. usa el contenedor de PostgreSQL que publique el puerto 5432 (o crea `barberia-db` si no hay
   ninguno) y dentro crea el usuario y la base `barberia`, con el esquema y las cuentas iniciales;
3. corre las pruebas del backend y lo arranca en `http://localhost:8080`;
4. corre las pruebas del frontend y lo arranca en `http://localhost:5173`.

Si usa un contenedor existente, pide el nombre de su superusuario (por defecto `postgres`). El
secreto de los tokens (`JWT_SECRETO`) se genera al azar para la sesión si no está definido.

## Cuentas iniciales

Contraseña de las tres cuentas: `Barberia2026` (cámbienla antes de usar datos reales).

| Rol | Correo |
| --- | --- |
| Dueño | dueno@barberia.com |
| Barbero | barbero1@barberia.com |
| Barbero | barbero2@barberia.com |

Los clientes crean su cuenta desde la pantalla de ingreso.

## Base de datos a mano

Con PostgreSQL en un contenedor (reemplacen `mi-postgres` por el nombre del contenedor):

```powershell
docker exec mi-postgres psql -U postgres -c "CREATE ROLE barberia LOGIN PASSWORD 'barberia'"
docker exec mi-postgres psql -U postgres -c "CREATE DATABASE barberia OWNER barberia"
docker cp database\barberia.sql mi-postgres:/tmp/barberia.sql
docker cp database\datos_iniciales.sql mi-postgres:/tmp/datos_iniciales.sql
docker exec mi-postgres psql -U barberia -d barberia -f /tmp/barberia.sql
docker exec mi-postgres psql -U barberia -d barberia -f /tmp/datos_iniciales.sql
```
