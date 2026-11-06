import { borrarToken, leerToken } from '../sesion/almacen';

/** Evento que se emite cuando el backend rechaza el token (vencido o inválido). */
export const EVENTO_SESION_EXPIRADA = 'barberia:sesion-expirada';

/** Error de la API con el estado HTTP y el cuerpo Problem Details del backend. */
export class ErrorApi extends Error {
  readonly estado: number;
  readonly datos: Record<string, unknown>;

  constructor(estado: number, mensaje: string, datos: Record<string, unknown> = {}) {
    super(mensaje);
    this.name = 'ErrorApi';
    this.estado = estado;
    this.datos = datos;
  }
}

type Metodo = 'GET' | 'POST' | 'PATCH';

interface Opciones {
  metodo?: Metodo;
  cuerpo?: unknown;
  parametros?: Record<string, string>;
}

const MENSAJES_POR_ESTADO: Record<number, string> = {
  400: 'Revisa los datos del formulario.',
  401: 'Tu sesión terminó. Inicia sesión de nuevo.',
  403: 'Tu cuenta no tiene acceso a esta sección.',
  404: 'No encontramos lo que buscas.',
  500: 'El servidor no pudo completar la operación. Intenta de nuevo en un momento.',
};

/** Hace una solicitud JSON al backend; agrega el token si hay sesión. */
export async function solicitar<T>(ruta: string, opciones: Opciones = {}): Promise<T> {
  const { metodo = 'GET', cuerpo, parametros } = opciones;
  const token = leerToken();

  const cabeceras: Record<string, string> = { Accept: 'application/json' };
  if (cuerpo !== undefined) {
    cabeceras['Content-Type'] = 'application/json';
  }
  if (token) {
    cabeceras.Authorization = `Bearer ${token}`;
  }
  const url = parametros ? `${ruta}?${new URLSearchParams(parametros).toString()}` : ruta;

  let respuesta: Response;
  try {
    respuesta = await fetch(url, {
      method: metodo,
      headers: cabeceras,
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ErrorApi(0, 'No hay conexión con el servidor. Revisa tu conexión e intenta de nuevo.');
  }

  if (respuesta.status === 401 && token) {
    borrarToken();
    window.dispatchEvent(new Event(EVENTO_SESION_EXPIRADA));
  }
  if (!respuesta.ok) {
    throw await aErrorApi(respuesta);
  }
  if (respuesta.status === 204) {
    return undefined as T;
  }
  return (await respuesta.json()) as T;
}

async function aErrorApi(respuesta: Response): Promise<ErrorApi> {
  let datos: Record<string, unknown> = {};
  try {
    datos = (await respuesta.json()) as Record<string, unknown>;
  } catch {
    // Respuesta sin cuerpo JSON (por ejemplo, el 401 del filtro de seguridad).
  }
  return new ErrorApi(respuesta.status, mensajeDe(respuesta.status, datos), datos);
}

function mensajeDe(estado: number, datos: Record<string, unknown>): string {
  const detalle = typeof datos.detail === 'string' ? datos.detail : undefined;
  // Los errores de validación de Spring llegan con un texto genérico en inglés.
  const esGenerico = detalle === undefined || detalle === 'Invalid request content.';
  if (!esGenerico) {
    return detalle;
  }
  return MENSAJES_POR_ESTADO[estado] ?? MENSAJES_POR_ESTADO[500]!;
}
