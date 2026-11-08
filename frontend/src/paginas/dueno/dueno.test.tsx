import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it } from 'vitest';
import type { CalendarioBarbero, Cita, ReporteGastos, Usuario } from '../../api/tipos';
import { guardarToken } from '../../sesion/almacen';
import { montarApp, simularApi, type RespuestaSimulada } from '../../pruebas/utilidades';
import { pesos } from '../../utilidades/dinero';
import { hoyISO, sumarDias } from '../../utilidades/fechas';
import { periodoPredefinido } from '../../utilidades/periodos';

const dueno: Usuario = { id: 1, nombre: 'Oscar Cruz', correo: 'dueno@barberia.com', telefono: null, rol: 'DUENO' };
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
  barberoNombre: 'Carlos',
  clienteId: 7,
  clienteNombre: 'Juan Pérez',
  ...cambios,
});

const sinGastos: ReporteGastos = { periodo: periodoPredefinido('mes'), totalGastos: 0, detalle: [] };

function api(extra: Record<string, RespuestaSimulada> = {}) {
  return simularApi({
    'GET /api/auth/yo': { cuerpo: dueno },
    'GET /api/citas': { cuerpo: [] },
    'GET /api/reportes/gastos': { cuerpo: sinGastos },
    ...extra,
  });
}

const consultas = (fetch: ReturnType<typeof simularApi>) => fetch.mock.calls.map(([url]) => String(url));

beforeEach(() => guardarToken('token-dueno'));

describe('operación del día (HU-06)', () => {
  it('muestra las citas de todos los barberos, cada uno en su columna', async () => {
    const calendario: CalendarioBarbero[] = [
      { barberoId: 3, barberoNombre: 'Andrés', citas: [] },
      {
        barberoId: 2,
        barberoNombre: 'Carlos',
        citas: [cita({}), cita({ id: 2, horaInicio: '11:00:00', clienteNombre: 'Pedro', estado: 'ATENDIDA' })],
      },
    ];
    api({ 'GET /api/citas': { cuerpo: calendario } });
    montarApp('/dueno');

    const carlos = await screen.findByRole('region', { name: 'Carlos' });
    expect(within(carlos).getByText('Juan Pérez')).toBeInTheDocument();
    expect(within(carlos).getByText('Corte de cabello · Atendida')).toBeInTheDocument();
    expect(within(screen.getByRole('region', { name: 'Andrés' })).getByText('Sin citas')).toBeInTheDocument();
    expect(screen.getByText('2 citas entre 2 barberos.')).toBeInTheDocument();
  });

  it('consulta otro día con el selector', async () => {
    const fetch = api();
    montarApp('/dueno');

    await userEvent.click(await screen.findByRole('button', { name: 'Día siguiente' }));

    await waitFor(() => expect(consultas(fetch)).toContain(`/api/citas?fecha=${sumarDias(hoy, 1)}`));
  });
});

describe('gastos (HU-14)', () => {
  it('registra el gasto y actualiza el resumen del mes', async () => {
    let registrado = false;
    const fetch = api();
    fetch.mockImplementation(async (url: string, init?: RequestInit) => {
      const clave = `${init?.method ?? 'GET'} ${url.split('?')[0]}`;
      if (clave === 'POST /api/gastos') {
        registrado = true;
        return new Response(JSON.stringify({ id: 4, concepto: 'Insumos', fecha: hoy, valor: 50000 }), { status: 201 });
      }
      if (clave === 'GET /api/reportes/gastos') {
        const detalle = registrado ? [{ concepto: 'Insumos', cantidad: 1, total: 50000 }] : [];
        return new Response(JSON.stringify({ ...sinGastos, totalGastos: registrado ? 50000 : 0, detalle }));
      }
      return new Response(JSON.stringify(dueno));
    });
    montarApp('/dueno/gastos');

    await userEvent.type(await screen.findByLabelText('Concepto'), 'Insumos');
    await userEvent.type(screen.getByLabelText('Valor'), '50.000');
    await userEvent.click(screen.getByRole('button', { name: 'Registrar gasto' }));

    expect(await screen.findByRole('status')).toHaveTextContent(`Gasto registrado: Insumos, ${pesos(50000)}.`);
    const envio = fetch.mock.calls.find(([, init]) => init?.method === 'POST')!;
    expect(JSON.parse(envio[1]!.body as string)).toEqual({ concepto: 'Insumos', fecha: hoy, valor: 50000 });
    expect(await screen.findByRole('cell', { name: 'Insumos' })).toBeInTheDocument();
  });

  it('valida los campos antes de enviar', async () => {
    const fetch = api();
    montarApp('/dueno/gastos');

    await userEvent.click(await screen.findByRole('button', { name: 'Registrar gasto' }));

    expect(screen.getByText('Escribe en qué se gastó, por ejemplo Insumos o Arriendo.')).toBeInTheDocument();
    expect(screen.getByText('Escribe el valor del gasto, por ejemplo 50000.')).toBeInTheDocument();
    expect(fetch.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false);
  });
});

describe('reportes (HU-15, HU-29, HU-16)', () => {
  const mes = periodoPredefinido('mes');

  it('la utilidad neta resta comisiones y gastos a los ingresos', async () => {
    const fetch = api({
      'GET /api/reportes/utilidad-neta': {
        cuerpo: { periodo: mes, ingresosBrutos: 100000, comisionesPagadas: 48000, gastosOperativos: 30000, utilidadNeta: 22000 },
      },
    });
    montarApp('/dueno/reportes');

    const reporte = await screen.findByRole('article', { name: 'Utilidad neta' });
    expect(within(reporte).getByText(`− ${pesos(48000)}`)).toBeInTheDocument();
    expect(within(reporte).getByText(pesos(22000))).toBeInTheDocument();
    expect(consultas(fetch)).toContain(`/api/reportes/utilidad-neta?desde=${mes.desde}&hasta=${mes.hasta}`);
  });

  it('el reporte de ingresos muestra el total y la cantidad de cortes', async () => {
    api({
      'GET /api/reportes/utilidad-neta': {
        cuerpo: { periodo: mes, ingresosBrutos: 0, comisionesPagadas: 0, gastosOperativos: 0, utilidadNeta: 0 },
      },
      'GET /api/reportes/ingresos': { cuerpo: { periodo: mes, cantidadCortes: 3, ingresosBrutos: 75000 } },
    });
    montarApp('/dueno/reportes');

    await userEvent.click(await screen.findByRole('radio', { name: 'Ingresos brutos' }));

    const reporte = await screen.findByRole('article', { name: 'Ingresos brutos' });
    expect(within(reporte).getByText(pesos(75000))).toBeInTheDocument();
    expect(within(reporte).getByText('3')).toBeInTheDocument();
  });

  it('el reporte de gastos muestra el detalle por concepto', async () => {
    api({
      'GET /api/reportes/utilidad-neta': {
        cuerpo: { periodo: mes, ingresosBrutos: 0, comisionesPagadas: 0, gastosOperativos: 0, utilidadNeta: 0 },
      },
      'GET /api/reportes/gastos': {
        cuerpo: {
          periodo: mes,
          totalGastos: 980000,
          detalle: [
            { concepto: 'Arriendo', cantidad: 1, total: 900000 },
            { concepto: 'Insumos', cantidad: 2, total: 80000 },
          ],
        },
      },
    });
    montarApp('/dueno/reportes');

    await userEvent.click(await screen.findByRole('radio', { name: 'Gastos operativos' }));

    const reporte = await screen.findByRole('article', { name: 'Gastos operativos' });
    expect(within(reporte).getByRole('cell', { name: 'Arriendo' })).toBeInTheDocument();
    expect(within(reporte).getByText(pesos(980000))).toBeInTheDocument();
  });

  it('con fechas invertidas avisa y no consulta', async () => {
    const fetch = api({
      'GET /api/reportes/utilidad-neta': {
        cuerpo: { periodo: mes, ingresosBrutos: 0, comisionesPagadas: 0, gastosOperativos: 0, utilidadNeta: 0 },
      },
    });
    montarApp('/dueno/reportes');

    await userEvent.click(await screen.findByRole('radio', { name: 'Otras fechas' }));
    const hasta = screen.getByLabelText('Hasta');
    await userEvent.clear(hasta);
    await userEvent.type(hasta, '2020-01-01');
    const antes = fetch.mock.calls.length;

    expect(await screen.findByText('La fecha final no puede ser anterior a la inicial.')).toBeInTheDocument();
    expect(screen.queryByRole('article')).not.toBeInTheDocument();
    expect(fetch.mock.calls.length).toBe(antes);
  });
});

describe('periodos', () => {
  it('el mes anterior va del primero al último día', () => {
    expect(periodoPredefinido('mes-anterior', '2026-03-15')).toEqual({ desde: '2026-02-01', hasta: '2026-02-28' });
    expect(periodoPredefinido('mes-anterior', '2026-01-10')).toEqual({ desde: '2025-12-01', hasta: '2025-12-31' });
    expect(periodoPredefinido('mes', '2026-11-06')).toEqual({ desde: '2026-11-01', hasta: '2026-11-06' });
  });
});
