import { useState } from 'react';
import { calendarioGeneral } from '../../api/negocio';
import { SelectorDia } from '../../componentes/SelectorDia';
import { hora, hoyISO } from '../../utilidades/fechas';
import { useCarga } from '../../utilidades/useCarga';

/**
 * HU-06: calendario consolidado del día. Cada barbero es una columna, como
 * las sillas del local, con sus citas ordenadas por hora.
 */
export function OperacionDia() {
  const [fecha, setFecha] = useState(hoyISO());
  const { datos, error, cargando } = useCarga(() => calendarioGeneral(fecha), [fecha]);
  const totalCitas = datos?.reduce((total, silla) => total + silla.citas.length, 0) ?? 0;

  return (
    <section className="pagina">
      <h1>Operación del día</h1>
      <SelectorDia fecha={fecha} alCambiar={setFecha} />

      {error && (
        <p className="aviso aviso--error" role="alert">
          {error}
        </p>
      )}
      {cargando && !datos && <p className="texto-tenue">Cargando el calendario…</p>}

      {datos && datos.length === 0 && (
        <div className="vacio">
          <p>Todavía no hay barberos registrados.</p>
        </div>
      )}

      {datos && datos.length > 0 && (
        <>
          <p className="texto-tenue">
            {totalCitas === 0
              ? 'No hay citas agendadas este día.'
              : `${totalCitas} ${totalCitas === 1 ? 'cita' : 'citas'} entre ${datos.length} barberos.`}
          </p>
          <div className="sillas">
            {datos.map((silla) => (
              <section key={silla.barberoId} className="silla" aria-labelledby={`silla-${silla.barberoId}`}>
                <h2 id={`silla-${silla.barberoId}`} className="silla__barbero">
                  {silla.barberoNombre}
                </h2>
                {silla.citas.length === 0 ? (
                  <p className="silla__libre">Sin citas</p>
                ) : (
                  <ol className="silla__citas">
                    {silla.citas.map((cita) => (
                      <li key={cita.id} className={`turno turno--${cita.estado.toLowerCase()}`}>
                        <span className="turno__hora">{hora(cita.horaInicio)}</span>
                        <span className="turno__cliente">{cita.clienteNombre ?? 'Cliente'}</span>
                        <span className="turno__estado">{cita.estado === 'ATENDIDA' ? 'Atendida' : 'Agendada'}</span>
                      </li>
                    ))}
                  </ol>
                )}
              </section>
            ))}
          </div>
        </>
      )}
    </section>
  );
}
