/**
 * Fechas como texto ISO (AAAA-MM-DD), igual que el backend. Se construyen con
 * la hora local para que "hoy" sea el día de la barbería y no el de UTC.
 */

function aISO(fecha: Date): string {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

function desdeISO(iso: string): Date {
  const [anio, mes, dia] = iso.split('-').map(Number);
  return new Date(anio!, mes! - 1, dia!);
}

export function hoyISO(): string {
  return aISO(new Date());
}

export function sumarDias(iso: string, dias: number): string {
  const fecha = desdeISO(iso);
  fecha.setDate(fecha.getDate() + dias);
  return aISO(fecha);
}

/** Próximos días a partir de hoy, para elegir la fecha de la cita. */
export function proximosDias(cantidad: number, desde = hoyISO()): string[] {
  return Array.from({ length: cantidad }, (_, i) => sumarDias(desde, i));
}

const DIA_SEMANA = new Intl.DateTimeFormat('es-CO', { weekday: 'short' });
const DIA_MES = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short' });
const MES_CORTO = new Intl.DateTimeFormat('es-CO', { month: 'short' });
const FECHA_LARGA = new Intl.DateTimeFormat('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });

const sinPunto = (texto: string) => texto.replace('.', '');

export function nombreDia(iso: string, hoy = hoyISO()): string {
  if (iso === hoy) return 'Hoy';
  if (iso === sumarDias(hoy, 1)) return 'Mañana';
  const texto = sinPunto(DIA_SEMANA.format(desdeISO(iso)));
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

export function diaYMes(iso: string): string {
  return sinPunto(DIA_MES.format(desdeISO(iso)));
}

/** Número del día del mes: "15". */
export function numeroDia(iso: string): string {
  return String(desdeISO(iso).getDate());
}

/** Día y mes con el mes en minúscula: "15 de nov". */
export function fechaCorta(iso: string): string {
  const fecha = desdeISO(iso);
  return `${fecha.getDate()} de ${sinPunto(MES_CORTO.format(fecha))}`;
}

/** Duración legible: 30 → "30 min", 60 → "1 h", 90 → "1 h 30 min". */
export function duracion(minutos: number): string {
  const horas = Math.floor(minutos / 60);
  const resto = minutos % 60;
  if (horas === 0) return `${resto} min`;
  return resto === 0 ? `${horas} h` : `${horas} h ${resto} min`;
}

export function fechaLarga(iso: string): string {
  const texto = FECHA_LARGA.format(desdeISO(iso));
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

/** El backend puede enviar "10:00" o "10:00:00"; se muestra siempre HH:mm. */
export function hora(valor: string): string {
  return valor.slice(0, 5);
}
