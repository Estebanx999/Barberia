import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it } from 'vitest';
import type { Cita, Usuario } from '../../api/tipos';
import { guardarToken } from '../../sesion/almacen';
import { montarApp, simularApi, type RespuestaSimulada } from '../../pruebas/utilidades';
import { hoyISO, sumarDias } from '../../utilidades/fechas';

const juan: Usuario = { id: 7, nombre: 'Juan Pérez', correo: 'juan@correo.com', telefono: '300', rol: 'CLIENTE' };
const barberos = [
  { id: 2, nombre: 'Carlos', descripcion: 'Especialista en degradados', fotoUrl: null },
  { id: 3, nombre: 'Andrés', descripcion: null, fotoUrl: null },
];
const servicios = [
  { codigo: 'CORTE', nombre: 'Corte de cabello', duracionMinutos: 30 },
  { codigo: 'BARBA', nombre: 'Perfilado de barba', duracionMinutos: 30 },
  { codigo: 'CORTE_Y_BARBA', nombre: 'Corte y barba', duracionMinutos: 60 },
];
const manana = sumarDias(hoyISO(), 1);

const cita = (cambios: Partial<Cita>): Cita => ({
  id: 1,
  fecha: manana,
  horaInicio: '10:00:00',
  horaFin: '10:30:00',
  servicio: 'CORTE',
  servicioNombre: 'Corte de cabello',
  estado: 'AGENDADA',
  barberoId: 2,
  barberoNombre: 'Carlos',
  clienteId: 7,
  clienteNombre: 'Juan Pérez',
  ...cambios,
});

function api(extra: Record<string, RespuestaSimulada> = {}) {
  return simularApi({
    'GET /api/auth/yo': { cuerpo: juan },
    'GET /api/barberos': { cuerpo: barberos },
    'GET /api/servicios': { cuerpo: servicios },
    'GET /api/citas/mias': { cuerpo: [] },
    ...extra,
  });
}

beforeEach(() => guardarToken('token-cliente'));

describe('agendar cita (HU-01, HU-02, HU-07)', () => {
  it('muestra el perfil de cada barbero sin su rol', async () => {
    api();
    montarApp('/cliente/agendar');

    const grupo = await screen.findByRole('group', { name: 'Elige tu barbero' });
    expect(within(grupo).getByText('Especialista en degradados')).toBeInTheDocument();
    // Sin foto se muestran las iniciales.
    expect(within(grupo).getByText('C')).toBeInTheDocument();
    expect(within(grupo).queryByText(/dueño/i)).not.toBeInTheDocument();
  });

  it('muestra solo las horas en que cabe el servicio con el barbero y el día elegidos', async () => {
    const fetch = api({
      'GET /api/barberos/2/disponibilidad': {
        cuerpo: [
          { horaInicio: '09:00:00', horaFin: '09:30:00' },
          { horaInicio: '10:30:00', horaFin: '11:00:00' },
        ],
      },
    });
    montarApp('/cliente/agendar');

    await userEvent.click(await screen.findByRole('radio', { name: 'Carlos' }));
    expect(screen.getByText('Elige un servicio para ver en qué horas cabe.')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('radio', { name: 'Corte y barba' }));

    const horas = await screen.findByRole('group', { name: 'Hora' });
    expect(within(horas).getAllByRole('radio').map((r) => (r as HTMLInputElement).value)).toEqual(['09:00', '10:30']);
    const consulta = fetch.mock.calls.find(([url]) => String(url).includes('/disponibilidad'))!;
    expect(consulta[0]).toBe(`/api/barberos/2/disponibilidad?fecha=${hoyISO()}&servicio=CORTE_Y_BARBA`);
  });

  it('agenda la cita y confirma en Mis citas', async () => {
    const fetch = api({
      'GET /api/barberos/2/disponibilidad': { cuerpo: [{ horaInicio: '10:00:00', horaFin: '10:30:00' }] },
      'POST /api/citas': { estado: 201, cuerpo: cita({}) },
    });
    montarApp('/cliente/agendar');

    await userEvent.click(await screen.findByRole('radio', { name: 'Carlos' }));
    await userEvent.click(screen.getByRole('radio', { name: 'Corte de cabello' }));
    await userEvent.click(screen.getByRole('radio', { name: /^Mañana/ }));
    await userEvent.click(await screen.findByRole('radio', { name: '10:00' }));
    expect(screen.getByText('Corte de cabello · 30 min con Carlos')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'Agendar cita' }));

    const confirmacion = await screen.findByRole('status', { name: 'Cita agendada' });
    expect(confirmacion).toHaveTextContent('¡Cita agendada con éxito!');
    expect(confirmacion).toHaveTextContent('Corte de cabello');
    expect(confirmacion).toHaveTextContent(/, 10:00/);
    const envio = fetch.mock.calls.find(([, init]) => init?.method === 'POST')!;
    expect(JSON.parse(envio[1]!.body as string)).toEqual({
      barberoId: 2,
      servicio: 'CORTE',
      fecha: manana,
      horaInicio: '10:00',
    });
  });

  it('si el horario se ocupó, muestra los que siguen libres', async () => {
    api({
      'GET /api/barberos/2/disponibilidad': { cuerpo: [{ horaInicio: '10:00:00', horaFin: '10:30:00' }] },
      'POST /api/citas': {
        estado: 409,
        cuerpo: {
          detail: 'El horario seleccionado no está disponible para este barbero',
          horariosDisponibles: [{ horaInicio: '11:00:00', horaFin: '11:30:00' }],
        },
      },
    });
    montarApp('/cliente/agendar');

    await userEvent.click(await screen.findByRole('radio', { name: 'Carlos' }));
    await userEvent.click(screen.getByRole('radio', { name: 'Corte de cabello' }));
    await userEvent.click(await screen.findByRole('radio', { name: '10:00' }));
    await userEvent.click(screen.getByRole('button', { name: 'Agendar cita' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Ese horario se acaba de ocupar');
    expect(screen.queryByRole('radio', { name: '10:00' })).not.toBeInTheDocument();
    expect(screen.getByRole('radio', { name: '11:00' })).not.toBeChecked();
    expect(screen.getByRole('button', { name: 'Agendar cita' })).toBeDisabled();
  });

  it('avisa cuando el barbero no tiene horarios libres ese día', async () => {
    api({ 'GET /api/barberos/3/disponibilidad': { cuerpo: [] } });
    montarApp('/cliente/agendar');

    await userEvent.click(await screen.findByRole('radio', { name: 'Andrés' }));
    await userEvent.click(screen.getByRole('radio', { name: 'Perfilado de barba' }));

    expect(
      await screen.findByText('No quedan horarios libres este día para este servicio. Prueba con otro día.'),
    ).toBeInTheDocument();
  });

  it('no permite agendar sin elegir la hora', async () => {
    api();
    montarApp('/cliente/agendar');

    expect(await screen.findByRole('radio', { name: 'Carlos' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Agendar cita' })).toBeDisabled();
    expect(screen.getByText('Elige un barbero para ver sus horarios libres.')).toBeInTheDocument();
  });
});

describe('mis citas (HU-24)', () => {
  it('separa las próximas del historial y muestra el servicio', async () => {
    api({
      'GET /api/citas/mias': {
        cuerpo: [
          cita({ id: 1, fecha: manana }),
          cita({ id: 2, fecha: sumarDias(hoyISO(), -3), estado: 'ATENDIDA', barberoNombre: 'Andrés' }),
        ],
      },
    });
    montarApp('/cliente');

    const proximas = await screen.findByRole('region', { name: 'Próximas (1)' });
    expect(within(proximas).getByText('Carlos')).toBeInTheDocument();
    expect(within(proximas).getByText(/^Corte de cabello · Mañana .*, 10:00$/)).toBeInTheDocument();
    expect(within(proximas).getByText('Confirmada')).toBeInTheDocument();
    const historial = screen.getByRole('region', { name: 'Historial' });
    expect(within(historial).getByText('Atendida')).toBeInTheDocument();
  });

  it('invita a agendar cuando no hay citas', async () => {
    api();
    montarApp('/cliente');

    expect(await screen.findByText(/Aún no tienes citas/)).toBeInTheDocument();
    expect(screen.getAllByRole('link', { name: 'Agendar cita' }).length).toBeGreaterThan(0);
  });
});
