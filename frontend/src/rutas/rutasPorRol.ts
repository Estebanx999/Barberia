import type { Rol } from '../api/tipos';

/** Panel al que llega cada rol al iniciar sesión (HU-22, HU-23, HU-24). */
export const INICIO_POR_ROL: Record<Rol, string> = {
  CLIENTE: '/cliente',
  BARBERO: '/barbero',
  DUENO: '/dueno',
};

export type IconoSeccion = 'inicio' | 'calendario' | 'mas' | 'monedas' | 'recibo' | 'grafica';

export interface Seccion {
  ruta: string;
  texto: string;
  icono: IconoSeccion;
}

/** Secciones del menú de cada rol. */
export const NAVEGACION: Record<Rol, Seccion[]> = {
  CLIENTE: [
    { ruta: '/cliente', texto: 'Mis citas', icono: 'calendario' },
    { ruta: '/cliente/agendar', texto: 'Agendar cita', icono: 'mas' },
  ],
  BARBERO: [
    { ruta: '/barbero', texto: 'Mi agenda', icono: 'calendario' },
    { ruta: '/barbero/sin-cita', texto: 'Corte sin cita', icono: 'mas' },
    { ruta: '/barbero/comisiones', texto: 'Mis comisiones', icono: 'monedas' },
  ],
  DUENO: [
    { ruta: '/dueno', texto: 'Operación del día', icono: 'inicio' },
    { ruta: '/dueno/mi-agenda', texto: 'Mi agenda', icono: 'calendario' },
    { ruta: '/dueno/sin-cita', texto: 'Corte sin cita', icono: 'mas' },
    { ruta: '/dueno/gastos', texto: 'Gastos', icono: 'recibo' },
    { ruta: '/dueno/reportes', texto: 'Reportes', icono: 'grafica' },
  ],
};

export const NOMBRE_ROL: Record<Rol, string> = {
  CLIENTE: 'Cliente',
  BARBERO: 'Barbero',
  DUENO: 'Dueño',
};
