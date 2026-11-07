import type { Periodo } from '../api/tipos';
import { hoyISO, sumarDias } from './fechas';

export type ClavePeriodo = 'mes' | 'mes-anterior' | 'personalizado';

export const NOMBRE_PERIODO: Record<ClavePeriodo, string> = {
  mes: 'Este mes',
  'mes-anterior': 'Mes anterior',
  personalizado: 'Otras fechas',
};

/** Rango de fechas de los atajos de periodo; ambos extremos incluidos, como en el backend. */
export function periodoPredefinido(clave: Exclude<ClavePeriodo, 'personalizado'>, hoy = hoyISO()): Periodo {
  const primeroDelMes = `${hoy.slice(0, 8)}01`;
  if (clave === 'mes') {
    return { desde: primeroDelMes, hasta: hoy };
  }
  const ultimoDelAnterior = sumarDias(primeroDelMes, -1);
  return { desde: `${ultimoDelAnterior.slice(0, 8)}01`, hasta: ultimoDelAnterior };
}

export function periodoValido(periodo: Periodo): boolean {
  return periodo.desde !== '' && periodo.hasta !== '' && periodo.desde <= periodo.hasta;
}
