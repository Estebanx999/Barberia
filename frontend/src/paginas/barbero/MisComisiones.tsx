import { useState } from 'react';
import { misCortes } from '../../api/atencion';
import type { Periodo } from '../../api/tipos';
import { GrupoOpciones } from '../../componentes/GrupoOpciones';
import { pesos } from '../../utilidades/dinero';
import { diaYMes, hoyISO, nombreDia, sumarDias } from '../../utilidades/fechas';
import { useCarga } from '../../utilidades/useCarga';

type ClavePeriodo = 'hoy' | 'semana' | 'mes';

function periodoDe(clave: ClavePeriodo, hoy = hoyISO()): Periodo {
  switch (clave) {
    case 'hoy':
      return { desde: hoy, hasta: hoy };
    case 'semana':
      return { desde: sumarDias(hoy, -6), hasta: hoy };
    case 'mes':
      return { desde: `${hoy.slice(0, 8)}01`, hasta: hoy };
  }
}

const NOMBRES: Record<ClavePeriodo, string> = {
  hoy: 'Hoy',
  semana: 'Últimos 7 días',
  mes: 'Este mes',
};

/** HU-23 / HU-26: el barbero ve únicamente sus propios cortes y comisiones. */
export function MisComisiones() {
  const [clave, setClave] = useState<ClavePeriodo>('semana');
  const periodo = periodoDe(clave);
  const { datos, error, cargando } = useCarga(() => misCortes(periodo.desde, periodo.hasta), [clave]);

  return (
    <section className="pagina">
      <h1>Mis comisiones</h1>

      <GrupoOpciones
        leyenda="Periodo"
        nombre="periodo"
        variante="fila"
        seleccionada={clave}
        alElegir={(valor) => setClave(valor as ClavePeriodo)}
        opciones={(Object.keys(NOMBRES) as ClavePeriodo[]).map((k) => ({ valor: k, contenido: NOMBRES[k] }))}
      />

      {error && (
        <p className="aviso aviso--error" role="alert">
          {error}
        </p>
      )}
      {cargando && !datos && <p className="texto-tenue">Calculando tus comisiones…</p>}

      {datos && (
        <>
          <dl className="cifras" aria-label="Resumen del periodo">
            <div>
              <dt>Cortes</dt>
              <dd>{datos.cantidadCortes}</dd>
            </div>
            <div>
              <dt>Total cobrado</dt>
              <dd>{pesos(datos.totalCobrado)}</dd>
            </div>
            <div className="cifras__destacada">
              <dt>Tu comisión</dt>
              <dd>{pesos(datos.totalComision)}</dd>
            </div>
          </dl>

          {datos.cortes.length === 0 ? (
            <div className="vacio">
              <p>No registraste cortes en este periodo.</p>
            </div>
          ) : (
            <table className="tabla">
              <caption className="solo-lectores">Cortes del periodo</caption>
              <thead>
                <tr>
                  <th scope="col">Fecha</th>
                  <th scope="col">Servicio</th>
                  <th scope="col">Cliente</th>
                  <th scope="col" className="numero">
                    Cobrado
                  </th>
                  <th scope="col" className="numero">
                    Comisión
                  </th>
                </tr>
              </thead>
              <tbody>
                {datos.cortes.map((corte) => (
                  <tr key={corte.id}>
                    <td>
                      {nombreDia(corte.fecha)} {diaYMes(corte.fecha)}
                    </td>
                    <td>
                      {corte.servicio}
                      {!corte.tieneCitaPrevia && <span className="etiqueta">Sin cita</span>}
                    </td>
                    <td>{corte.clienteNombre ?? '—'}</td>
                    <td className="numero" data-etiqueta="Cobrado">
                      {pesos(corte.valorCobrado)}
                    </td>
                    <td className="numero" data-etiqueta="Comisión">
                      {pesos(corte.comision.montoBarbero)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </section>
  );
}
