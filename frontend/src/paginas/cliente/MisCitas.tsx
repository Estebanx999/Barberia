import { Link, useLocation } from 'react-router-dom';
import { listarBarberos, misCitas } from '../../api/agenda';
import type { Barbero, Cita } from '../../api/tipos';
import { Avatar } from '../../componentes/Avatar';
import { IconoCheck } from '../../componentes/Iconos';
import { fechaCorta, fechaLarga, hoyISO, hora, nombreDia } from '../../utilidades/fechas';
import { useCarga } from '../../utilidades/useCarga';

/** HU-24: el cliente ve únicamente sus propias citas. HU-01: confirma la cita recién agendada. */
export function MisCitas() {
  const ubicacion = useLocation();
  const citaAgendada = (ubicacion.state as { citaAgendada?: Cita } | null)?.citaAgendada;
  const { datos, error, cargando } = useCarga(misCitas, []);
  // Solo para mostrar la foto de cada barbero; si falla, se ven sus iniciales.
  const barberos = useCarga(listarBarberos, []);
  const fotoDe = (cita: Cita) => barberos.datos?.find((b: Barbero) => b.id === cita.barberoId)?.fotoUrl;

  const hoy = hoyISO();
  const proximas = (datos ?? [])
    .filter((c) => c.estado === 'AGENDADA' && c.fecha >= hoy)
    .sort((a, b) => (a.fecha + a.horaInicio).localeCompare(b.fecha + b.horaInicio));
  const historial = (datos ?? []).filter((c) => !proximas.includes(c));

  return (
    <section className="pagina">
      <div className="pagina__titulo">
        <div className="pagina__cabecera">
          <h1>Mis citas</h1>
          <p className="texto-tenue">Tus próximas reservas y tu historial.</p>
        </div>
        <Link to="/cliente/agendar" className="boton boton--principal">
          Agendar cita
        </Link>
      </div>

      {citaAgendada && <CitaConfirmada cita={citaAgendada} />}
      {error && (
        <p className="aviso aviso--error" role="alert">
          {error}
        </p>
      )}
      {cargando && <p className="texto-tenue">Cargando tus citas…</p>}

      {datos && datos.length === 0 && (
        <div className="vacio">
          <p>Aún no tienes citas. Elige barbero, servicio y hora, y asegura tu turno.</p>
        </div>
      )}

      {proximas.length > 0 && <ListaCitas titulo={`Próximas (${proximas.length})`} citas={proximas} fotoDe={fotoDe} />}
      {historial.length > 0 && <ListaCitas titulo="Historial" citas={historial} fotoDe={fotoDe} tenue />}
    </section>
  );
}

function CitaConfirmada({ cita }: { cita: Cita }) {
  return (
    <section className="confirmacion" role="status" aria-label="Cita agendada">
      <span className="confirmacion__icono">
        <IconoCheck />
      </span>
      <h2>¡Cita agendada con éxito!</h2>
      <dl className="confirmacion__datos">
        <div>
          <dt>Barbero</dt>
          <dd>{cita.barberoNombre}</dd>
        </div>
        <div>
          <dt>Servicio</dt>
          <dd>{cita.servicioNombre}</dd>
        </div>
        <div>
          <dt>Fecha y hora</dt>
          <dd>
            {fechaLarga(cita.fecha)}, {hora(cita.horaInicio)}
          </dd>
        </div>
      </dl>
      <p className="confirmacion__nota">Se validó la disponibilidad sin cruces de horario.</p>
    </section>
  );
}

function ListaCitas({
  titulo,
  citas,
  fotoDe,
  tenue = false,
}: {
  titulo: string;
  citas: Cita[];
  fotoDe: (cita: Cita) => string | null | undefined;
  tenue?: boolean;
}) {
  return (
    <section className="bloque" aria-label={titulo}>
      <h2>{titulo}</h2>
      <ul className={`citas${tenue ? ' citas--tenues' : ''}`}>
        {citas.map((cita) => (
          <li key={cita.id} className="cita">
            <Avatar nombre={cita.barberoNombre} fotoUrl={fotoDe(cita)} />
            <span className="cita__detalle">
              <span className="cita__titulo">{cita.barberoNombre}</span>
              <span className="texto-tenue">
                {cita.servicioNombre} · {nombreDia(cita.fecha)} {fechaCorta(cita.fecha)}, {hora(cita.horaInicio)}
              </span>
            </span>
            <span className={`estado estado--${cita.estado.toLowerCase()}`}>
              {cita.estado === 'AGENDADA' ? 'Confirmada' : 'Atendida'}
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}
