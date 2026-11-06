import { Link, useLocation } from 'react-router-dom';
import { misCitas } from '../../api/agenda';
import type { Cita } from '../../api/tipos';
import { diaYMes, fechaLarga, hoyISO, hora, nombreDia } from '../../utilidades/fechas';
import { useCarga } from '../../utilidades/useCarga';

/** HU-24: el cliente ve únicamente sus propias citas. HU-01: confirma la cita recién agendada. */
export function MisCitas() {
  const ubicacion = useLocation();
  const citaAgendada = (ubicacion.state as { citaAgendada?: Cita } | null)?.citaAgendada;
  const { datos, error, cargando } = useCarga(misCitas, []);

  const hoy = hoyISO();
  const proximas = (datos ?? [])
    .filter((c) => c.estado === 'AGENDADA' && c.fecha >= hoy)
    .sort((a, b) => (a.fecha + a.horaInicio).localeCompare(b.fecha + b.horaInicio));
  const anteriores = (datos ?? []).filter((c) => !proximas.includes(c));

  return (
    <section className="pagina">
      <div className="pagina__titulo">
        <h1>Mis citas</h1>
        <Link to="/cliente/agendar" className="boton boton--principal">
          Agendar cita
        </Link>
      </div>

      {citaAgendada && (
        <p className="aviso aviso--exito" role="status">
          Tu cita quedó agendada: {fechaLarga(citaAgendada.fecha).toLowerCase()} a las {hora(citaAgendada.horaInicio)} con{' '}
          {citaAgendada.barberoNombre}.
        </p>
      )}
      {error && (
        <p className="aviso aviso--error" role="alert">
          {error}
        </p>
      )}
      {cargando && <p className="texto-tenue">Cargando tus citas…</p>}

      {datos && datos.length === 0 && (
        <div className="vacio">
          <p>Aún no tienes citas. Elige barbero y hora, y asegura tu turno.</p>
        </div>
      )}

      {proximas.length > 0 && <ListaCitas titulo="Próximas" citas={proximas} />}
      {anteriores.length > 0 && <ListaCitas titulo="Anteriores" citas={anteriores} tenue />}
    </section>
  );
}

function ListaCitas({ titulo, citas, tenue = false }: { titulo: string; citas: Cita[]; tenue?: boolean }) {
  return (
    <section className="bloque" aria-label={titulo}>
      <h2>{titulo}</h2>
      <ul className={`citas${tenue ? ' citas--tenues' : ''}`}>
        {citas.map((cita) => (
          <li key={cita.id} className="cita">
            <span className="cita__dia">
              <span className="dia__nombre">{nombreDia(cita.fecha)}</span>
              <span className="dia__numero">{diaYMes(cita.fecha)}</span>
            </span>
            <span className="cita__detalle">
              <span className="cita__hora">
                {hora(cita.horaInicio)} – {hora(cita.horaFin)}
              </span>
              <span className="texto-tenue">con {cita.barberoNombre}</span>
            </span>
            <span className={`estado estado--${cita.estado.toLowerCase()}`}>
              {cita.estado === 'AGENDADA' ? 'Agendada' : 'Atendida'}
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}
