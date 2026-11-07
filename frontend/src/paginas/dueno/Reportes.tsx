import { useState } from 'react';
import { reporteGastos, reporteIngresos, reporteUtilidadNeta } from '../../api/negocio';
import type { Periodo, ReporteGastos, ReporteIngresos, ReporteUtilidadNeta } from '../../api/tipos';
import { Campo } from '../../componentes/Campo';
import { GrupoOpciones } from '../../componentes/GrupoOpciones';
import { pesos } from '../../utilidades/dinero';
import { diaYMes } from '../../utilidades/fechas';
import { NOMBRE_PERIODO, periodoPredefinido, periodoValido, type ClavePeriodo } from '../../utilidades/periodos';
import { useCarga } from '../../utilidades/useCarga';
import { TablaConceptos } from './Gastos';

type TipoReporte = 'utilidad' | 'ingresos' | 'gastos';

const NOMBRE_REPORTE: Record<TipoReporte, string> = {
  utilidad: 'Utilidad neta',
  ingresos: 'Ingresos brutos',
  gastos: 'Gastos operativos',
};

type Resultado =
  | { tipo: 'utilidad'; datos: ReporteUtilidadNeta }
  | { tipo: 'ingresos'; datos: ReporteIngresos }
  | { tipo: 'gastos'; datos: ReporteGastos };

function generar(tipo: TipoReporte, periodo: Periodo): Promise<Resultado> {
  switch (tipo) {
    case 'utilidad':
      return reporteUtilidadNeta(periodo).then((datos) => ({ tipo, datos }));
    case 'ingresos':
      return reporteIngresos(periodo).then((datos) => ({ tipo, datos }));
    case 'gastos':
      return reporteGastos(periodo).then((datos) => ({ tipo, datos }));
  }
}

/** HU-15, HU-29 y HU-16: los tres reportes financieros sobre un mismo periodo. */
export function Reportes() {
  const [tipo, setTipo] = useState<TipoReporte>('utilidad');
  const [clave, setClave] = useState<ClavePeriodo>('mes');
  const [personalizado, setPersonalizado] = useState<Periodo>(periodoPredefinido('mes'));

  const periodo = clave === 'personalizado' ? personalizado : periodoPredefinido(clave);
  const valido = periodoValido(periodo);
  const reporte = useCarga(valido ? () => generar(tipo, periodo) : null, [tipo, periodo.desde, periodo.hasta]);

  return (
    <section className="pagina">
      <h1>Reportes</h1>

      <div className="filtros">
        <GrupoOpciones
          leyenda="Reporte"
          nombre="reporte"
          variante="fila"
          seleccionada={tipo}
          alElegir={(valor) => setTipo(valor as TipoReporte)}
          opciones={(Object.keys(NOMBRE_REPORTE) as TipoReporte[]).map((t) => ({ valor: t, contenido: NOMBRE_REPORTE[t] }))}
        />
        <GrupoOpciones
          leyenda="Periodo"
          nombre="periodo"
          variante="fila"
          seleccionada={clave}
          alElegir={(valor) => setClave(valor as ClavePeriodo)}
          opciones={(Object.keys(NOMBRE_PERIODO) as ClavePeriodo[]).map((k) => ({ valor: k, contenido: NOMBRE_PERIODO[k] }))}
        />
        {clave === 'personalizado' && (
          <div className="rango">
            <Campo
              etiqueta="Desde"
              type="date"
              value={personalizado.desde}
              onChange={(e) => setPersonalizado({ ...personalizado, desde: e.target.value })}
            />
            <Campo
              etiqueta="Hasta"
              type="date"
              value={personalizado.hasta}
              onChange={(e) => setPersonalizado({ ...personalizado, hasta: e.target.value })}
              error={valido ? undefined : 'La fecha final no puede ser anterior a la inicial.'}
            />
          </div>
        )}
      </div>

      {reporte.error && (
        <p className="aviso aviso--error" role="alert">
          {reporte.error}
        </p>
      )}
      {reporte.cargando && <p className="texto-tenue">Generando el reporte…</p>}

      {valido && !reporte.cargando && reporte.datos && reporte.datos.tipo === tipo && (
        <article className="reporte" aria-label={NOMBRE_REPORTE[tipo]}>
          <p className="reporte__periodo">
            Del {diaYMes(periodo.desde)} al {diaYMes(periodo.hasta)}
          </p>
          <ContenidoReporte resultado={reporte.datos} />
        </article>
      )}
    </section>
  );
}

function ContenidoReporte({ resultado }: { resultado: Resultado }) {
  switch (resultado.tipo) {
    case 'ingresos':
      return (
        <dl className="cifras">
          <div className="cifras__destacada">
            <dt>Ingresos brutos</dt>
            <dd>{pesos(resultado.datos.ingresosBrutos)}</dd>
          </div>
          <div>
            <dt>Cortes realizados</dt>
            <dd>{resultado.datos.cantidadCortes}</dd>
          </div>
        </dl>
      );
    case 'gastos':
      return resultado.datos.detalle.length === 0 ? (
        <p className="texto-tenue">No hay gastos registrados en este periodo.</p>
      ) : (
        <TablaConceptos reporte={resultado.datos} />
      );
    case 'utilidad': {
      const { ingresosBrutos, comisionesPagadas, gastosOperativos, utilidadNeta } = resultado.datos;
      return (
        <dl className="cuenta">
          <div className="cuenta__fila">
            <dt>Ingresos brutos</dt>
            <dd>{pesos(ingresosBrutos)}</dd>
          </div>
          <div className="cuenta__fila">
            <dt>Comisiones pagadas a barberos</dt>
            <dd>− {pesos(comisionesPagadas)}</dd>
          </div>
          <div className="cuenta__fila">
            <dt>Gastos operativos</dt>
            <dd>− {pesos(gastosOperativos)}</dd>
          </div>
          <div className={`cuenta__fila cuenta__total${utilidadNeta < 0 ? ' cuenta__total--negativa' : ''}`}>
            <dt>Utilidad neta</dt>
            <dd>{pesos(utilidadNeta)}</dd>
          </div>
        </dl>
      );
    }
  }
}
