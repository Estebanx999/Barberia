import { describe, expect, it, vi } from 'vitest';
import { guardarToken, leerToken } from '../sesion/almacen';
import { simularApi } from '../pruebas/utilidades';
import { ErrorApi, EVENTO_SESION_EXPIRADA, solicitar } from './cliente';

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
});
