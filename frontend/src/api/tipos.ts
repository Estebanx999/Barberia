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

export interface DatosRegistro {
  nombre: string;
  correo: string;
  telefono: string;
  contrasena: string;
}
