import { describe, expect, it, vi } from 'vitest';
import { guardarToken, leerToken } from '../sesion/almacen';
import { simularApi } from '../pruebas/utilidades';
import { ErrorApi, EVENTO_SERVIDOR_INICIANDO, EVENTO_SESION_EXPIRADA, solicitar } from './cliente';

describe('cliente de la API', () => {
  it('envía el token de la sesión en la cabecera Authorization', async () => {
    guardarToken('abc');
    const fetch = simularApi({ 'GET /api/auth/yo': { cuerpo: { id: 1 } } });

    await solicitar('/api/auth/yo');

    const cabeceras = fetch.mock.calls[0]![1]!.headers as Record<string, string>;
    expect(cabeceras.Authorization).toBe('Bearer abc');
  });

  it('convierte el detalle del backend en el mensaje del error', async () => {
    simularApi({
      'POST /api/auth/login': { estado: 401, cuerpo: { status: 401, detail: 'Correo o contraseña incorrectos' } },
    });

    const error = await solicitar('/api/auth/login', { metodo: 'POST', cuerpo: {} }).catch((e: unknown) => e);

    expect(error).toBeInstanceOf(ErrorApi);
    expect((error as ErrorApi).estado).toBe(401);
    expect((error as ErrorApi).message).toBe('Correo o contraseña incorrectos');
  });

  it('traduce el mensaje genérico de validación de Spring', async () => {
    simularApi({ 'POST /api/citas': { estado: 400, cuerpo: { detail: 'Invalid request content.' } } });

    await expect(solicitar('/api/citas', { metodo: 'POST', cuerpo: {} })).rejects.toThrow(
      'Revisa los datos del formulario.',
    );
  });

  it('cierra la sesión cuando el backend rechaza el token', async () => {
    guardarToken('vencido');
    simularApi({ 'GET /api/auth/yo': { estado: 401 } });
    const alExpirar = vi.fn();
    window.addEventListener(EVENTO_SESION_EXPIRADA, alExpirar);

    await expect(solicitar('/api/auth/yo')).rejects.toBeInstanceOf(ErrorApi);

    expect(leerToken()).toBeNull();
    expect(alExpirar).toHaveBeenCalledOnce();
    window.removeEventListener(EVENTO_SESION_EXPIRADA, alExpirar);
  });

  it('informa cuando no hay conexión con el servidor', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));

    await expect(solicitar('/api/auth/yo')).rejects.toMatchObject({ estado: 0 });
  });

  describe('mientras el servidor de la nube se inicia', () => {
    /** Respuesta de la plataforma (HTML, no JSON) cuando el backend aún no arranca. */
    const iniciando = () => new Response('<html>Service waking up</html>', { status: 502 });
    const lista = () =>
      new Response(JSON.stringify({ id: 1 }), { status: 200, headers: { 'Content-Type': 'application/json' } });

    it('repite la solicitud hasta que el backend responde y avisa mientras espera', async () => {
      vi.useFakeTimers();
      const fetch = vi
        .fn()
        .mockResolvedValueOnce(iniciando())
        .mockResolvedValueOnce(iniciando())
        .mockResolvedValue(lista());
      vi.stubGlobal('fetch', fetch);
      const avisos: boolean[] = [];
      const alAvisar = (e: Event) => avisos.push((e as CustomEvent<boolean>).detail);
      window.addEventListener(EVENTO_SERVIDOR_INICIANDO, alAvisar);

      const resultado = solicitar('/api/auth/registro', { metodo: 'POST', cuerpo: {} });
      await vi.advanceTimersByTimeAsync(10_000);

      await expect(resultado).resolves.toEqual({ id: 1 });
      expect(fetch).toHaveBeenCalledTimes(3);
      expect(avisos).toEqual([true, false]);
      window.removeEventListener(EVENTO_SERVIDOR_INICIANDO, alAvisar);
      vi.useRealTimers();
    });

    it('no repite un 502 que viene del backend con su propio mensaje', async () => {
      simularApi({ 'POST /api/citas': { estado: 502, cuerpo: { detail: 'Fallo del proveedor' } } });

      await expect(solicitar('/api/citas', { metodo: 'POST', cuerpo: {} })).rejects.toThrow('Fallo del proveedor');
    });

    it('se rinde tras varios minutos sin respuesta', async () => {
      vi.useFakeTimers();
      vi.stubGlobal('fetch', vi.fn().mockImplementation(async () => iniciando()));

      const resultado = solicitar('/api/servicios').catch((e: unknown) => e);
      await vi.advanceTimersByTimeAsync(5 * 60_000);

      expect(await resultado).toMatchObject({ estado: 502 });
      vi.useRealTimers();
    });
  });
});
