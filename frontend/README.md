# Frontend – Sistema de gestión de barbería (MVP)

React 19 · TypeScript · Vite · React Router

## Requisitos
- Node.js 20 o superior
- El backend corriendo en `http://localhost:8080`

## Instalación y ejecución
```bash
npm install
npm run dev
```
La app queda en `http://localhost:5173`. Las solicitudes a `/api` se reenvían al backend
(otra dirección con la variable `VITE_BACKEND_URL`).

## Scripts
| Comando | Qué hace |
| --- | --- |
| `npm run dev` | servidor de desarrollo |
| `npm test` | pruebas (Vitest + Testing Library) |
| `npm run typecheck` | revisión de tipos |
| `npm run build` | compilación para producción en `dist/` |

## Sesión
El token del backend se guarda en `sessionStorage`: se mantiene al recargar y se borra al
cerrar la pestaña. Si el backend lo rechaza, la sesión se cierra y se vuelve al ingreso.
Las rutas por rol (HU-26) solo ordenan la navegación; la protección real la hace el backend.

## Estructura
```
src
├── api          cliente HTTP y llamadas al backend
├── sesion       estado de la sesión y almacenamiento del token
├── rutas        rutas protegidas por rol
├── componentes  piezas reutilizables (marco, campos, portada)
├── paginas      pantallas
├── estilos      estilos globales y tokens de diseño
└── pruebas      configuración y utilidades de pruebas
```
