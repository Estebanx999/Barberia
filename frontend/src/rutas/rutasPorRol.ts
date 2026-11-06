import type { Rol } from '../api/tipos';

/** Panel al que llega cada rol al iniciar sesión (HU-22, HU-23, HU-24). */
export const INICIO_POR_ROL: Record<Rol, string> = {
  CLIENTE: '/cliente',
  BARBERO: '/barbero',
  DUENO: '/dueno',
};

export const NOMBRE_ROL: Record<Rol, string> = {
  CLIENTE: 'Cliente',
  BARBERO: 'Barbero',
  DUENO: 'Dueño',
};
