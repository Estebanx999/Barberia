import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it } from 'vitest';
import type { Cita, Corte, ResumenCortes, Usuario } from '../../api/tipos';
import { guardarToken } from '../../sesion/almacen';
import { montarApp, simularApi, type RespuestaSimulada } from '../../pruebas/utilidades';
import { leerValor, pesos } from '../../utilidades/dinero';
import { hoyISO, sumarDias } from '../../utilidades/fechas';

const carlos: Usuario = { id: 2, nombre: 'Carlos Ramírez', correo: 'c@b.com', telefono: null, rol: 'BARBERO' };
const hoy = hoyISO();

const cita = (cambios: Partial<Cita>): Cita => ({
  id: 1,
  fecha: hoy,
  horaInicio: '10:00:00',
  horaFin: '10:30:00',
  servicio: 'CORTE',
  servicioNombre: 'Corte de cabello',
  estado: 'AGENDADA',
  barberoId: 2,
  barberoNombre: 'Carlos Ramírez',
  clienteId: 7,
  clienteNombre: 'Juan Pérez',
  ...cambios,
});

const corte = (cambios: Partial<Corte>): Corte => ({
  id: 20,
  fecha: hoy,
  servicio: 'Corte clásico',
  valorCobrado: 30000,
  tieneCitaPrevia: true,
  citaId: 1,
  barberoId: 2,
  barberoNombre: 'Carlos Ramírez',
  clienteId: 7,
  clienteNombre: 'Juan Pérez',
  comision: { montoBarbero: 18000, montoDueno: 12000, porcentajeBarbero: 60, porcentajeDueno: 40 },
  ...cambios,
});

const resumen = (cortes: Corte[]): ResumenCortes => ({
  periodo: { desde: hoy, hasta: hoy },
  cantidadCortes: cortes.length,
  totalCobrado: cortes.reduce((t, c) => t + c.valorCobrado, 0),
  totalComision: cortes.reduce((t, c) => t + c.comision.montoBarbero, 0),
  cortes,
});

function api(extra: Record<string, RespuestaSimulada> = {}) {
  return simularApi({
    'GET /api/auth/yo': { cuerpo: carlos },
    'GET /api/barberos/2/citas': { cuerpo: [] },
    'GET /api/cortes/mios': { cuerpo: resumen([]) },
    ...extra,
  });
}

beforeEach(() => guardarToken('token-barbero'));

describe('agenda del barbero (HU-05, HU-12, HU-27)', () => {
  it('muestra las citas del día con el nombre del cliente', async () => {
    const fetch = api({
      'GET /api/barberos/2/citas': {
        cuerpo: [cita({}), cita({ id: 2, horaInicio: '11:00:00', horaFin: '11:30:00', clienteNombre: 'Pedro' })],
      },
    });
    montarApp('/barbero');

    expect(await screen.findByRole('listitem', { name: '10:00 Juan Pérez' })).toBeInTheDocument();
    expect(screen.getByRole('listitem', { name: '11:00 Pedro' })).toBeInTheDocument();
    expect(fetch.mock.calls.some(([url]) => url === `/api/barberos/2/citas?fecha=${hoy}`)).toBe(true);
  });

  it('marca la cita como atendida y luego habilita el cobro', async () => {
    let atendida = false;
    const fetch = simularApi({
      'GET /api/auth/yo': { cuerpo: carlos },
      'GET /api/cortes/mios': { cuerpo: resumen([]) },
      'PATCH /api/citas/1/atendida': { cuerpo: cita({ estado: 'ATENDIDA' }) },
    });
    // La agenda cambia después de marcar la cita.
    fetch.mockImplementation(async (url: string, init?: RequestInit) => {
      const clave = `${init?.method ?? 'GET'} ${url.split('?')[0]}`;
      const cuerpos: Record<string, unknown> = {
        'GET /api/auth/yo': carlos,
        'GET /api/cortes/mios': resumen([]),
        'GET /api/barberos/2/citas': [cita({ estado: atendida ? 'ATENDIDA' : 'AGENDADA' })],
        'PATCH /api/citas/1/atendida': cita({ estado: 'ATENDIDA' }),
      };
      if (clave === 'PATCH /api/citas/1/atendida') atendida = true;
      return new Response(JSON.stringify(cuerpos[clave]), { status: 200 });
    });
    montarApp('/barbero');

    await userEvent.click(await screen.findByRole('button', { name: 'Marcar atendida' }));

    expect(await screen.findByRole('status')).toHaveTextContent('marcada como atendida');
    expect(await screen.findByRole('button', { name: 'Registrar cobro' })).toBeInTheDocument();
  });

  it('registra el cobro de una cita atendida y muestra la comisión', async () => {
    const fetch = api({
      'GET /api/barberos/2/citas': { cuerpo: [cita({ estado: 'ATENDIDA' })] },
      'POST /api/citas/1/corte': { estado: 201, cuerpo: corte({}) },
    });
    montarApp('/barbero');

    await userEvent.click(await screen.findByRole('button', { name: 'Registrar cobro' }));
    const formulario = screen.getByRole('form', { name: 'Cobro de la cita de Juan Pérez' });
    // El servicio llega lleno con el que reservó el cliente; el barbero puede ajustarlo.
    expect(within(formulario).getByLabelText('Servicio')).toHaveValue('Corte de cabello');
    await userEvent.clear(within(formulario).getByLabelText('Servicio'));
    await userEvent.type(within(formulario).getByLabelText('Servicio'), 'Corte clásico');
    await userEvent.type(within(formulario).getByLabelText('Valor cobrado'), '30.000');
    await userEvent.click(within(formulario).getByRole('button', { name: 'Guardar cobro' }));

    expect(await screen.findByRole('status')).toHaveTextContent(
      `Cobro registrado: ${pesos(30000)}. Tu parte: ${pesos(18000)}.`,
    );
    const envio = fetch.mock.calls.find(([, init]) => init?.method === 'POST')!;
    expect(JSON.parse(envio[1]!.body as string)).toEqual({ servicio: 'Corte clásico', valorCobrado: 30000 });
  });

  it('valida el cobro antes de enviarlo', async () => {
    const fetch = api({ 'GET /api/barberos/2/citas': { cuerpo: [cita({ estado: 'ATENDIDA' })] } });
    montarApp('/barbero');

    await userEvent.click(await screen.findByRole('button', { name: 'Registrar cobro' }));
    await userEvent.clear(screen.getByLabelText('Servicio'));
    await userEvent.click(screen.getByRole('button', { name: 'Guardar cobro' }));

    expect(screen.getByText('Escribe el servicio que hiciste.')).toBeInTheDocument();
    expect(screen.getByText('Escribe el valor cobrado, por ejemplo 30000.')).toBeInTheDocument();
    expect(fetch.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false);
  });

  it('una cita ya cobrada muestra el valor y no permite cobrar otra vez', async () => {
    api({
      'GET /api/barberos/2/citas': { cuerpo: [cita({ estado: 'ATENDIDA' })] },
      'GET /api/cortes/mios': { cuerpo: resumen([corte({})]) },
    });
    montarApp('/barbero');

    expect(await screen.findByText(`Cobrado ${pesos(30000)}`)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Registrar cobro' })).not.toBeInTheDocument();
  });

  it('una cita futura no se puede marcar como atendida', async () => {
    const manana = sumarDias(hoy, 1);
    api({ 'GET /api/barberos/2/citas': { cuerpo: [cita({ fecha: manana })] } });
    montarApp('/barbero');

    await userEvent.click(await screen.findByRole('button', { name: 'Día siguiente' }));

    expect(await screen.findByText('Agendada')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Marcar atendida' })).not.toBeInTheDocument();
  });
});

describe('corte sin cita (HU-28)', () => {
  const llenarCobro = async () => {
    await userEvent.type(screen.getByLabelText('Servicio'), 'Barba');
    await userEvent.type(screen.getByLabelText('Valor cobrado'), '15000');
  };

  it('busca al cliente registrado y registra el corte con su id', async () => {
    const fetch = api({
      'GET /api/clientes': { cuerpo: [{ id: 7, nombre: 'Juan Pérez', telefono: '3001112233' }] },
      'POST /api/cortes': {
        estado: 201,
        cuerpo: corte({ servicio: 'Barba', valorCobrado: 15000, tieneCitaPrevia: false, citaId: null,
          comision: { montoBarbero: 9000, montoDueno: 6000, porcentajeBarbero: 60, porcentajeDueno: 40 } }),
      },
    });
    montarApp('/barbero/sin-cita');

    await userEvent.type(await screen.findByLabelText('Buscar por nombre o teléfono'), 'juan');
    await userEvent.click(await screen.findByRole('radio', { name: 'Juan Pérez' }));
    await llenarCobro();
    await userEvent.click(screen.getByRole('button', { name: 'Registrar corte' }));

    expect(await screen.findByRole('heading', { name: 'Corte registrado' })).toBeInTheDocument();
    expect(screen.getByText(pesos(9000))).toBeInTheDocument();
    const envio = fetch.mock.calls.find(([, init]) => init?.method === 'POST')!;
    expect(JSON.parse(envio[1]!.body as string)).toEqual({ servicio: 'Barba', valorCobrado: 15000, clienteId: 7 });
  });

  it('registra al cliente nuevo con su nombre y teléfono', async () => {
    const fetch = api({ 'POST /api/cortes': { estado: 201, cuerpo: corte({ tieneCitaPrevia: false, citaId: null }) } });
    montarApp('/barbero/sin-cita');

    await userEvent.click(await screen.findByRole('radio', { name: 'Es nuevo' }));
    await userEvent.type(screen.getByLabelText('Nombre del cliente'), 'Pedro Gómez');
    await userEvent.type(screen.getByLabelText('Teléfono (opcional)'), '3009998877');
    await llenarCobro();
    await userEvent.click(screen.getByRole('button', { name: 'Registrar corte' }));

    await screen.findByRole('heading', { name: 'Corte registrado' });
    const envio = fetch.mock.calls.find(([, init]) => init?.method === 'POST')!;
    expect(JSON.parse(envio[1]!.body as string)).toEqual({
      servicio: 'Barba',
      valorCobrado: 15000,
      clienteNombre: 'Pedro Gómez',
      clienteTelefono: '3009998877',
    });
  });

  it('pide elegir al cliente antes de registrar', async () => {
    const fetch = api();
    montarApp('/barbero/sin-cita');

    await screen.findByRole('heading', { name: 'Registrar corte sin cita' });
    await llenarCobro();
    await userEvent.click(screen.getByRole('button', { name: 'Registrar corte' }));

    expect(screen.getByText('Busca y elige al cliente, o regístralo como cliente nuevo.')).toBeInTheDocument();
    expect(fetch.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false);
  });
});

describe('mis comisiones (HU-23)', () => {
  it('muestra el total cobrado, la comisión y el detalle del periodo', async () => {
    const fetch = api({
      'GET /api/cortes/mios': {
        cuerpo: resumen([corte({}), corte({ id: 21, servicio: 'Barba', valorCobrado: 20000, tieneCitaPrevia: false,
          comision: { montoBarbero: 12000, montoDueno: 8000, porcentajeBarbero: 60, porcentajeDueno: 40 } })]),
      },
    });
    montarApp('/barbero/comisiones');

    expect(await screen.findByText(pesos(50000))).toBeInTheDocument();
    expect(screen.getByText(pesos(30000), { selector: 'dd' })).toBeInTheDocument();
    expect(screen.getByText('Sin cita')).toBeInTheDocument();
    const consulta = fetch.mock.calls.find(([url]) => String(url).startsWith('/api/cortes/mios'))!;
    expect(consulta[0]).toBe(`/api/cortes/mios?desde=${sumarDias(hoy, -6)}&hasta=${hoy}`);
  });

  it('cambia de periodo', async () => {
    const fetch = api();
    montarApp('/barbero/comisiones');

    await userEvent.click(await screen.findByRole('radio', { name: 'Hoy' }));

    await waitFor(() =>
      expect(fetch.mock.calls.some(([url]) => url === `/api/cortes/mios?desde=${hoy}&hasta=${hoy}`)).toBe(true),
    );
    expect(await screen.findByText('No registraste cortes en este periodo.')).toBeInTheDocument();
  });
});

describe('valores en pesos', () => {
  it('interpreta lo que escribe el barbero', () => {
    expect(leerValor('30000')).toBe(30000);
    expect(leerValor('$ 30.000')).toBe(30000);
    expect(leerValor('0')).toBeNull();
    expect(leerValor('abc')).toBeNull();
  });
});
