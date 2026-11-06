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
