import { ErrorApi, solicitar } from './cliente';
import type { Barbero, Cita, CodigoServicio, Franja, Servicio, SolicitudCita } from './tipos';

export function listarBarberos(): Promise<Barbero[]> {
  return solicitar<Barbero[]>('/api/barberos');
}

export function listarServicios(): Promise<Servicio[]> {
  return solicitar<Servicio[]>('/api/servicios');
}

/** HU-02: horas del día en las que el barbero puede atender el servicio elegido. */
export function horariosDisponibles(barberoId: number, fecha: string, servicio: CodigoServicio): Promise<Franja[]> {
  return solicitar<Franja[]>(`/api/barberos/${barberoId}/disponibilidad`, { parametros: { fecha, servicio } });
}

/** HU-01 / HU-07 */
export function agendarCita(solicitud: SolicitudCita): Promise<Cita> {
  return solicitar<Cita>('/api/citas', { metodo: 'POST', cuerpo: solicitud });
}

/** HU-24 */
export function misCitas(): Promise<Cita[]> {
  return solicitar<Cita[]>('/api/citas/mias');
}

/** Si el horario se ocupó (409), el backend envía los horarios que siguen libres. */
export function horariosRestantes(error: unknown): Franja[] | null {
  if (error instanceof ErrorApi && error.estado === 409 && Array.isArray(error.datos.horariosDisponibles)) {
    return error.datos.horariosDisponibles as Franja[];
  }
  return null;
}
