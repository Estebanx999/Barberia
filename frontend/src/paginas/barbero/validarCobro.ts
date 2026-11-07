import type { Cobro } from '../../api/tipos';
import type { ErroresCobro, ValoresCobro } from '../../componentes/CamposCobro';
import { leerValor } from '../../utilidades/dinero';

/** Devuelve el cobro listo para enviar, o los errores de cada campo. */
export function validarCobro(valores: ValoresCobro): { cobro: Cobro } | { errores: ErroresCobro } {
  const errores: ErroresCobro = {};
  const servicio = valores.servicio.trim();
  const valor = leerValor(valores.valor);
  if (!servicio) errores.servicio = 'Escribe el servicio que hiciste.';
  if (valor === null) errores.valor = 'Escribe el valor cobrado, por ejemplo 30000.';
  if (Object.keys(errores).length > 0 || valor === null) {
    return { errores };
  }
  return { cobro: { servicio, valorCobrado: valor } };
}
