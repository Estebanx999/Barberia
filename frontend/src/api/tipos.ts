/** Tipos de las respuestas del backend (mismos nombres de campo que la API). */

export type Rol = 'CLIENTE' | 'BARBERO' | 'DUENO';

export interface Usuario {
  id: number;
  nombre: string;
  correo: string | null;
  telefono: string | null;
  rol: Rol;
}

export interface SesionIniciada {
  token: string;
  usuario: Usuario;
}

export interface Barbero {
  id: number;
  nombre: string;
}

/** Turno dentro de la jornada; horas en formato HH:mm o HH:mm:ss. */
export interface Franja {
  horaInicio: string;
  horaFin: string;
}

export type EstadoCita = 'AGENDADA' | 'ATENDIDA';

export interface Cita {
  id: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: EstadoCita;
  barberoId: number;
  barberoNombre: string;
  clienteId: number;
  clienteNombre: string | null;
}

export interface SolicitudCita {
  barberoId: number;
  fecha: string;
  horaInicio: string;
}

export interface DatosRegistro {
  nombre: string;
  correo: string;
  telefono: string;
  contrasena: string;
}

export interface Comision {
  montoBarbero: number;
  montoDueno: number;
  porcentajeBarbero: number;
  porcentajeDueno: number;
}

export interface Corte {
  id: number;
  fecha: string;
  servicio: string;
  valorCobrado: number;
  tieneCitaPrevia: boolean;
  citaId: number | null;
  barberoId: number;
  barberoNombre: string;
  clienteId: number;
  clienteNombre: string | null;
  comision: Comision;
}

export interface Periodo {
  desde: string;
  hasta: string;
}

export interface ResumenCortes {
  periodo: Periodo;
  cantidadCortes: number;
  totalCobrado: number;
  totalComision: number;
  cortes: Corte[];
}

export interface ClienteResumen {
  id: number;
  nombre: string;
  telefono: string | null;
}

export interface Cobro {
  servicio: string;
  valorCobrado: number;
}

/** Cliente de un corte sin cita: uno ya registrado (clienteId) o uno nuevo. */
export interface CorteSinCita extends Cobro {
  clienteId?: number;
  clienteNombre?: string;
  clienteTelefono?: string;
}
