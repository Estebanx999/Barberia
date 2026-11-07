import { solicitar } from './cliente';
import type {
  CalendarioBarbero,
  DatosGasto,
  Gasto,
  Periodo,
  ReporteGastos,
  ReporteIngresos,
  ReporteUtilidadNeta,
} from './tipos';

/** HU-06: citas de todos los barberos en el día. */
export function calendarioGeneral(fecha: string): Promise<CalendarioBarbero[]> {
  return solicitar<CalendarioBarbero[]>('/api/citas', { parametros: { fecha } });
}

/** HU-14 */
export function registrarGasto(gasto: DatosGasto): Promise<Gasto> {
  return solicitar<Gasto>('/api/gastos', { metodo: 'POST', cuerpo: gasto });
}

const comoParametros = (periodo: Periodo) => ({ desde: periodo.desde, hasta: periodo.hasta });

/** HU-15 */
export function reporteIngresos(periodo: Periodo): Promise<ReporteIngresos> {
  return solicitar<ReporteIngresos>('/api/reportes/ingresos', { parametros: comoParametros(periodo) });
}

/** HU-29 */
export function reporteGastos(periodo: Periodo): Promise<ReporteGastos> {
  return solicitar<ReporteGastos>('/api/reportes/gastos', { parametros: comoParametros(periodo) });
}

/** HU-16 */
export function reporteUtilidadNeta(periodo: Periodo): Promise<ReporteUtilidadNeta> {
  return solicitar<ReporteUtilidadNeta>('/api/reportes/utilidad-neta', { parametros: comoParametros(periodo) });
}
