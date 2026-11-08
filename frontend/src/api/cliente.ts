import { borrarToken, leerToken } from '../sesion/almacen';

/** Evento que se emite cuando el backend rechaza el token (vencido o inválido). */
export const EVENTO_SESION_EXPIRADA = 'barberia:sesion-expirada';

/**
 * Evento que avisa si el servidor se está iniciando (detail: true) o ya respondió
 * (detail: false). En la nube gratuita el backend se apaga sin uso y tarda unos
 * minutos en volver; mientras tanto la plataforma responde 502/503 sin JSON.
 */
export const EVENTO_SERVIDOR_INICIANDO = 'barberia:servidor-iniciando';

const ESTADOS_SERVIDOR_INICIANDO = new Set([502, 503]);
const PAUSA_ENTRE_INTENTOS_MS = 5_000;
const ESPERA_MAXIMA_MS = 4 * 60_000;

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
  502: 'El servidor no está disponible en este momento. Intenta de nuevo en unos minutos.',
  503: 'El servidor no está disponible en este momento. Intenta de nuevo en unos minutos.',
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

  const enviar = () =>
    fetch(url, {
      method: metodo,
      headers: cabeceras,
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    });

  let respuesta: Response;
  try {
    respuesta = await esperarServidor(enviar);
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

/**
 * Repite la solicitud mientras la plataforma indique que el backend se está
 * iniciando. Esa respuesta no viene del backend (no trae JSON), así que la
 * solicitud no se procesó y repetirla no la duplica.
 */
async function esperarServidor(enviar: () => Promise<Response>): Promise<Response> {
  const limite = Date.now() + ESPERA_MAXIMA_MS;
  let respuesta = await enviar();
  if (!esServidorIniciando(respuesta)) {
    return respuesta;
  }
  window.dispatchEvent(new CustomEvent(EVENTO_SERVIDOR_INICIANDO, { detail: true }));
  try {
    while (esServidorIniciando(respuesta) && Date.now() < limite) {
      await new Promise((resolver) => setTimeout(resolver, PAUSA_ENTRE_INTENTOS_MS));
      respuesta = await enviar();
    }
    return respuesta;
  } finally {
    window.dispatchEvent(new CustomEvent(EVENTO_SERVIDOR_INICIANDO, { detail: false }));
  }
}

function esServidorIniciando(respuesta: Response): boolean {
  const tipo = respuesta.headers.get('Content-Type') ?? '';
  return ESTADOS_SERVIDOR_INICIANDO.has(respuesta.status) && !tipo.includes('json');
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
