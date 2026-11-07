import type { Rol } from '../api/tipos';

/** Panel al que llega cada rol al iniciar sesión (HU-22, HU-23, HU-24). */
export const INICIO_POR_ROL: Record<Rol, string> = {
  CLIENTE: '/cliente',
  BARBERO: '/barbero',
  DUENO: '/dueno',
};

/** Secciones del menú de cada rol; se completan módulo a módulo. */
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
  DUENO: [],
};

export const NOMBRE_ROL: Record<Rol, string> = {
  CLIENTE: 'Cliente',
  BARBERO: 'Barbero',
  DUENO: 'Dueño',
};
