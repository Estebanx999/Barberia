import { solicitar } from './cliente';
import type { Cita, ClienteResumen, Cobro, Corte, CorteSinCita, ResumenCortes } from './tipos';

/** HU-05: citas del barbero en el día, ordenadas por hora. */
export function calendarioBarbero(barberoId: number, fecha: string): Promise<Cita[]> {
  return solicitar<Cita[]>(`/api/barberos/${barberoId}/citas`, { parametros: { fecha } });
}

/** HU-12 */
export function marcarAtendida(citaId: number): Promise<Cita> {
  return solicitar<Cita>(`/api/citas/${citaId}/atendida`, { metodo: 'PATCH' });
}

/** HU-27 + HU-13 */
export function registrarCobro(citaId: number, cobro: Cobro): Promise<Corte> {
  return solicitar<Corte>(`/api/citas/${citaId}/corte`, { metodo: 'POST', cuerpo: cobro });
}

/** HU-28 + HU-13 */
export function registrarCorteSinCita(datos: CorteSinCita): Promise<Corte> {
  return solicitar<Corte>('/api/cortes', { metodo: 'POST', cuerpo: datos });
}

/** HU-23: cortes y comisiones del barbero autenticado. */
export function misCortes(desde: string, hasta: string): Promise<ResumenCortes> {
  return solicitar<ResumenCortes>('/api/cortes/mios', { parametros: { desde, hasta } });
}

/** HU-28: clientes registrados por nombre o teléfono. */
export function buscarClientes(texto: string): Promise<ClienteResumen[]> {
  return solicitar<ClienteResumen[]>('/api/clientes', { parametros: { buscar: texto } });
}
