import type { Rol } from '../api/tipos';

/** Panel al que llega cada rol al iniciar sesión (HU-22, HU-23, HU-24). */
export const INICIO_POR_ROL: Record<Rol, string> = {
  CLIENTE: '/cliente',
  BARBERO: '/barbero',
  DUENO: '/dueno',
};

/** Secciones del menú de cada rol. */
export const NAVEGACION: Record<Rol, { ruta: string; texto: string }[]> = {
  CLIENTE: [
    { ruta: '/cliente', texto: 'Mis citas' },
    { ruta: '/cliente/agendar', texto: 'Agendar cita' },
  ],
  BARBERO: [
    { ruta: '/barbero', texto: 'Mi agenda' },
    { ruta: '/barbero/sin-cita', texto: 'Corte sin cita' },
    { ruta: '/barbero/comisiones', texto: 'Mis comisiones' },
  ],
  DUENO: [
    { ruta: '/dueno', texto: 'Operación del día' },
    { ruta: '/dueno/mi-agenda', texto: 'Mi agenda' },
    { ruta: '/dueno/sin-cita', texto: 'Corte sin cita' },
    { ruta: '/dueno/gastos', texto: 'Gastos' },
    { ruta: '/dueno/reportes', texto: 'Reportes' },
  ],
};

export const NOMBRE_ROL: Record<Rol, string> = {
  CLIENTE: 'Cliente',
  BARBERO: 'Barbero',
  DUENO: 'Dueño',
};
