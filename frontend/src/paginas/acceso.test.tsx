import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import type { Rol, Usuario } from '../api/tipos';
import { guardarToken, leerToken } from '../sesion/almacen';
import { montarApp, simularApi } from '../pruebas/utilidades';
import { validarRegistro } from './Registro';

const usuario = (rol: Rol, nombre = 'Juan Pérez'): Usuario => ({
  id: 7,
  nombre,
  correo: 'juan@correo.com',
  telefono: '3001112233',
  rol,
});

describe('inicio de sesión (HU-22, HU-23, HU-24)', () => {
  it('lleva al cliente a sus citas', async () => {
    simularApi({
      'POST /api/auth/login': { cuerpo: { token: 'abc', usuario: usuario('CLIENTE') } },
      'GET /api/citas/mias': { cuerpo: [] },
    });
    montarApp('/ingresar');

    await userEvent.type(screen.getByLabelText('Correo'), 'juan@correo.com');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'secreta123');
    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }));

    expect(await screen.findByRole('heading', { name: 'Mis citas' })).toBeInTheDocument();
    expect(leerToken()).toBe('abc');
  });

  it('lleva al barbero a su agenda', async () => {
    simularApi({
      'POST /api/auth/login': { cuerpo: { token: 'abc', usuario: usuario('BARBERO') } },
      'GET /api/barberos/7/citas': { cuerpo: [] },
      'GET /api/cortes/mios': { cuerpo: { periodo: {}, cantidadCortes: 0, totalCobrado: 0, totalComision: 0, cortes: [] } },
    });
    montarApp('/ingresar');

    await userEvent.type(screen.getByLabelText('Correo'), 'juan@correo.com');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'secreta123');
    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }));

    expect(await screen.findByRole('heading', { name: 'Mi agenda' })).toBeInTheDocument();
  });

  it('lleva al dueño a su panel', async () => {
    simularApi({ 'POST /api/auth/login': { cuerpo: { token: 'abc', usuario: usuario('DUENO') } } });
    montarApp('/ingresar');

    await userEvent.type(screen.getByLabelText('Correo'), 'juan@correo.com');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'secreta123');
    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }));

    expect(await screen.findByText(/Desde aquí ves la operación del día/)).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Hola, Juan' })).toBeInTheDocument();
    expect(leerToken()).toBe('abc');
  });

  it('muestra el error del backend si las credenciales no son válidas', async () => {
    simularApi({ 'POST /api/auth/login': { estado: 401, cuerpo: { detail: 'Correo o contraseña incorrectos' } } });
    montarApp('/ingresar');

    await userEvent.type(screen.getByLabelText('Correo'), 'juan@correo.com');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'mala');
    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Correo o contraseña incorrectos');
    expect(leerToken()).toBeNull();
  });

  it('pide los dos campos antes de enviar', async () => {
    const fetch = simularApi({});
    montarApp('/ingresar');

    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }));

    expect(screen.getByRole('alert')).toHaveTextContent('Escribe tu correo y tu contraseña.');
    expect(fetch).not.toHaveBeenCalled();
  });

  it('cerrar sesión borra el token y vuelve al ingreso', async () => {
    guardarToken('abc');
    simularApi({ 'GET /api/auth/yo': { cuerpo: usuario('CLIENTE') }, 'GET /api/citas/mias': { cuerpo: [] } });
    montarApp('/cliente');

    await userEvent.click(await screen.findByRole('button', { name: 'Cerrar sesión' }));

    expect(await screen.findByRole('heading', { name: 'Iniciar sesión' })).toBeInTheDocument();
    expect(leerToken()).toBeNull();
  });
});

describe('registro de cliente (HU-21)', () => {
  const llenar = async () => {
    await userEvent.type(screen.getByLabelText('Nombre'), 'Juan Pérez');
    await userEvent.type(screen.getByLabelText('Correo'), 'juan@correo.com');
    await userEvent.type(screen.getByLabelText('Teléfono'), '3001112233');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'secreta123');
  };

  it('crea la cuenta e inicia la sesión del cliente', async () => {
    const fetch = simularApi({
      'POST /api/auth/registro': { estado: 201, cuerpo: usuario('CLIENTE') },
      'POST /api/auth/login': { cuerpo: { token: 'nuevo', usuario: usuario('CLIENTE') } },
      'GET /api/citas/mias': { cuerpo: [] },
    });
    montarApp('/registro');

    await llenar();
    await userEvent.click(screen.getByRole('button', { name: 'Crear cuenta' }));

    expect(await screen.findByRole('heading', { name: 'Mis citas' })).toBeInTheDocument();
    const cuerpo = JSON.parse(fetch.mock.calls[0]![1]!.body as string) as Record<string, string>;
    expect(cuerpo).toEqual({
      nombre: 'Juan Pérez',
      correo: 'juan@correo.com',
      telefono: '3001112233',
      contrasena: 'secreta123',
    });
  });

  it('avisa en el campo de correo si ya existe una cuenta', async () => {
    simularApi({ 'POST /api/auth/registro': { estado: 409, cuerpo: { detail: 'El correo ya está registrado' } } });
    montarApp('/registro');

    await llenar();
    await userEvent.click(screen.getByRole('button', { name: 'Crear cuenta' }));

    expect(await screen.findByText('Ya hay una cuenta con este correo. Inicia sesión con ella.')).toBeInTheDocument();
    expect(screen.getByLabelText('Correo')).toHaveAttribute('aria-invalid', 'true');
  });

  it('valida los datos con las mismas reglas del backend', () => {
    expect(validarRegistro({ nombre: ' ', correo: 'no', telefono: '', contrasena: 'corta' })).toEqual({
      nombre: expect.any(String),
      correo: expect.any(String),
      telefono: expect.any(String),
      contrasena: 'Usa al menos 8 caracteres.',
    });
    expect(validarRegistro({ nombre: 'Juan', correo: 'j@c.co', telefono: '300', contrasena: 'secreta123' })).toEqual({});
  });
});

describe('acceso por rol (HU-26)', () => {
  it('sin sesión, una página protegida lleva al ingreso', async () => {
    simularApi({});
    montarApp('/dueno');
    expect(await screen.findByRole('heading', { name: 'Iniciar sesión' })).toBeInTheDocument();
  });

  it('un cliente no entra al panel del dueño', async () => {
    guardarToken('abc');
    simularApi({ 'GET /api/auth/yo': { cuerpo: usuario('CLIENTE') } });
    montarApp('/dueno');

    expect(
      await screen.findByRole('heading', { name: 'Esta sección no está disponible para tu cuenta' }),
    ).toBeInTheDocument();
  });

  it('un token guardado que ya no sirve se descarta', async () => {
    guardarToken('vencido');
    simularApi({ 'GET /api/auth/yo': { estado: 401 } });
    montarApp('/cliente');

    expect(await screen.findByRole('heading', { name: 'Iniciar sesión' })).toBeInTheDocument();
    await waitFor(() => expect(leerToken()).toBeNull());
  });
});
