import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';
import { App } from '../App';
import { SesionProvider } from '../sesion/SesionContext';

export interface RespuestaSimulada {
  estado?: number;
  cuerpo?: unknown;
}

/**
 * Reemplaza fetch: cada solicitud "MÉTODO /ruta" recibe la respuesta indicada.
 * Devuelve el mock para revisar qué se envió.
 */
export function simularApi(respuestas: Record<string, RespuestaSimulada>) {
  const mock = vi.fn(async (url: string, init?: RequestInit) => {
    const clave = `${init?.method ?? 'GET'} ${url.split('?')[0]}`;
    const respuesta = respuestas[clave];
    if (!respuesta) {
      throw new Error(`Solicitud no simulada: ${clave}`);
    }
    const estado = respuesta.estado ?? 200;
    return new Response(respuesta.cuerpo === undefined ? null : JSON.stringify(respuesta.cuerpo), {
      status: estado,
      headers: { 'Content-Type': 'application/json' },
    });
  });
  vi.stubGlobal('fetch', mock);
  return mock;
}

/** Monta la aplicación completa en la ruta indicada. */
export function montarApp(ruta: string) {
  return render(
    <MemoryRouter initialEntries={[ruta]}>
      <SesionProvider>
        <App />
      </SesionProvider>
    </MemoryRouter>,
  );
}
