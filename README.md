# Barbería

Sistema de gestión de citas y control contable para una barbería (MVP, Release 1).

| Carpeta | Contenido |
| --- | --- |
| `backend` | API REST en Java 17 con Spring Boot (ver `backend/README.md`) |
| `frontend` | Aplicación web en React con TypeScript (ver `frontend/README.md`) |
| `database` | Esquema PostgreSQL y cuentas iniciales |
| `docker-compose.yml` | Despliegue completo con Docker |
| `render.yaml` | Publicación en la nube (Render) |

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
Si la base se creó con una versión anterior, actualícenla sin perder datos:

```powershell
docker compose cp database\actualizar_servicio_y_perfil.sql db:/tmp/actualizar.sql
docker compose exec db psql -U barberia -d barberia -f /tmp/actualizar.sql
docker compose restart backend
```

o empiecen de cero con `docker compose down -v` y `docker compose up -d --build`.

## Publicación en la nube (Render)

`render.yaml` describe lo que se crea en Render: una base PostgreSQL 16, el backend y el
frontend, cada uno con su Dockerfile. El frontend reenvía `/api` al backend, como en local.

1. Suban el código a GitHub (Render lo toma de la rama principal).
2. En [dashboard.render.com](https://dashboard.render.com): **New → Blueprint**, conecten el
   repositorio y elijan esta carpeta. Render pedirá `BACKEND_URL`: escriban
   `https://barberia-backend.onrender.com`.
3. Cuando la base esté lista, copien su **External Database URL** (base `barberia-db` →
   *Connect*) y carguen el esquema y las cuentas iniciales desde esta carpeta:

   ```powershell
   docker run --rm -v "${PWD}\database:/sql" postgres:16 psql "URL_EXTERNA" -f /sql/barberia.sql -f /sql/datos_iniciales.sql
   ```

4. En el servicio `barberia-backend` usen **Manual Deploy → Restart service** para que arranque
   con la base ya creada.
5. La aplicación queda en la dirección del servicio `barberia-web`.

Si Render le asigna al backend otra dirección (por ejemplo con un sufijo), corrijan
`BACKEND_URL` en *Environment* del servicio `barberia-web`.

En el plan gratuito los servicios se duermen tras 15 minutos sin uso (la primera visita tarda
cerca de un minuto) y la base de datos vence a los 30 días.

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

| Rol | Nombre | Correo |
| --- | --- | --- |
| Dueño | Mateo Herrera | dueno@barberia.com |
| Barbero | Carlos Ramírez | barbero1@barberia.com |
| Barbero | Luis Peña | barbero2@barberia.com |

Los clientes crean su cuenta desde la pantalla de ingreso. Al agendar ven el perfil de cada
barbero (nombre, descripción y foto). Para ponerle foto a un barbero se guarda el enlace de la
imagen; sin foto se muestran sus iniciales:

```sql
UPDATE usuario SET foto_url = 'https://...' WHERE correo = 'barbero1@barberia.com';
```

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
