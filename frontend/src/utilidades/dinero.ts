import type { Comision, Rol } from '../api/tipos';

const PESOS = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

export function pesos(valor: number): string {
  // Intl separa "$" y la cifra con un espacio duro; se normaliza a espacio simple.
  return PESOS.format(valor).replace(/ /g, ' ');
}

/** Interpreta lo que escribe el barbero ("30000", "30.000", "$ 30.000"); null si no es un valor válido. */
export function leerValor(texto: string): number | null {
  const digitos = texto.replace(/\D/g, '');
  if (digitos === '') return null;
  const valor = Number(digitos);
  return valor > 0 ? valor : null;
}

/** Lo que le corresponde a quien hizo el corte: el barbero recibe su comisión; el dueño, su parte. */
export function parteDe(rol: Rol, comision: Comision): number {
  return rol === 'DUENO' ? comision.montoDueno : comision.montoBarbero;
}
