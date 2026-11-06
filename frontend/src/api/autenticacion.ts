import { solicitar } from './cliente';
import type { DatosRegistro, SesionIniciada, Usuario } from './tipos';

/** HU-22, HU-23, HU-24 */
export function iniciarSesion(correo: string, contrasena: string): Promise<SesionIniciada> {
  return solicitar<SesionIniciada>('/api/auth/login', { metodo: 'POST', cuerpo: { correo, contrasena } });
}

/** HU-21 */
export function registrarCliente(datos: DatosRegistro): Promise<Usuario> {
  return solicitar<Usuario>('/api/auth/registro', { metodo: 'POST', cuerpo: datos });
}

export function obtenerUsuarioActual(): Promise<Usuario> {
  return solicitar<Usuario>('/api/auth/yo');
}
