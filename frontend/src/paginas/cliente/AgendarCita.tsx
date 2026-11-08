import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { agendarCita, horariosDisponibles, horariosRestantes, listarBarberos, listarServicios } from '../../api/agenda';
import { ErrorApi } from '../../api/cliente';
import type { CodigoServicio, Franja } from '../../api/tipos';
import { Avatar } from '../../componentes/Avatar';
import { GrupoOpciones } from '../../componentes/GrupoOpciones';
import { IconoReloj } from '../../componentes/Iconos';
import { duracion, fechaCorta, hora, nombreDia, numeroDia, proximosDias } from '../../utilidades/fechas';
import { useCarga } from '../../utilidades/useCarga';

const DIAS_VISIBLES = 14;

/**
 * HU-01: agendar con el barbero de preferencia el servicio que se quiere.
 * HU-02: solo se ofrecen las horas en las que ese servicio cabe sin cruces.
 * HU-07: si el horario se ocupó mientras tanto, el backend lo rechaza y se
 * muestran los horarios que siguen libres.
 */
export function AgendarCita() {
  const navegar = useNavigate();
  const [dias] = useState(() => proximosDias(DIAS_VISIBLES));
  const [barberoId, setBarberoId] = useState<number | null>(null);
  const [servicioElegido, setServicioElegido] = useState<CodigoServicio | null>(null);
  const [fecha, setFecha] = useState<string>(dias[0]!);
  const [horaElegida, setHoraElegida] = useState<string | null>(null);
  const [horariosTrasConflicto, setHorariosTrasConflicto] = useState<Franja[] | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const barberos = useCarga(listarBarberos, []);
  const servicios = useCarga(listarServicios, []);
  const horarios = useCarga(
    barberoId === null || servicioElegido === null
      ? null
      : () => horariosDisponibles(barberoId, fecha, servicioElegido),
    [barberoId, servicioElegido, fecha],
  );
  const franjas = horariosTrasConflicto ?? horarios.datos ?? [];
  const barbero = barberos.datos?.find((b) => b.id === barberoId);
  const servicio = servicios.datos?.find((s) => s.codigo === servicioElegido);

  function reiniciarHora() {
    setHoraElegida(null);
    setHorariosTrasConflicto(null);
    setAviso(null);
  }

  async function confirmar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (barberoId === null || servicioElegido === null || horaElegida === null) {
      return;
    }
    setEnviando(true);
    setAviso(null);
    try {
      const cita = await agendarCita({ barberoId, servicio: servicioElegido, fecha, horaInicio: horaElegida });
      navegar('/cliente', { state: { citaAgendada: cita } });
    } catch (causa) {
      const restantes = horariosRestantes(causa);
      if (restantes) {
        setHorariosTrasConflicto(restantes);
        setHoraElegida(null);
        setAviso('Ese horario se acaba de ocupar. Elige otro de los que siguen libres.');
      } else {
        setAviso(causa instanceof ErrorApi ? causa.message : 'No se pudo agendar la cita. Intenta de nuevo.');
      }
      setEnviando(false);
    }
  }

  const faltante =
    barberoId === null
      ? 'Elige un barbero para ver sus horarios libres.'
      : servicioElegido === null
        ? 'Elige un servicio para ver en qué horas cabe.'
        : null;

  return (
    <section className="pagina">
      <div className="pagina__cabecera">
        <h1>Agendar cita</h1>
        <p className="texto-tenue">Elige quién te atiende, el servicio y la hora que te queda mejor.</p>
      </div>

      <form className="agendar" onSubmit={confirmar}>
        {barberos.error && (
          <p className="aviso aviso--error" role="alert">
            {barberos.error}
          </p>
        )}
        {barberos.cargando && <p className="texto-tenue">Cargando barberos…</p>}
        {barberos.datos && barberos.datos.length === 0 && (
          <p className="texto-tenue">Por ahora no hay barberos disponibles para agendar.</p>
        )}
        {barberos.datos && barberos.datos.length > 0 && (
          <GrupoOpciones
            leyenda="Elige tu barbero"
            nombre="barbero"
            variante="tarjetas"
            seleccionada={barberoId === null ? null : String(barberoId)}
            alElegir={(valor) => {
              setBarberoId(Number(valor));
              reiniciarHora();
            }}
            opciones={barberos.datos.map((b) => ({
              valor: String(b.id),
              etiqueta: b.nombre,
              contenido: (
                <span className="perfil-barbero">
                  <Avatar nombre={b.nombre} fotoUrl={b.fotoUrl} tamano="grande" />
                  <span className="perfil-barbero__texto">
                    <span className="perfil-barbero__nombre">{b.nombre}</span>
                    {b.descripcion && <span className="perfil-barbero__descripcion">{b.descripcion}</span>}
                  </span>
                </span>
              ),
            }))}
          />
        )}

        {servicios.error && (
          <p className="aviso aviso--error" role="alert">
            {servicios.error}
          </p>
        )}
        {servicios.datos && (
          <GrupoOpciones
            leyenda="Selecciona el servicio"
            nombre="servicio"
            variante="tarjetas"
            seleccionada={servicioElegido}
            alElegir={(valor) => {
              setServicioElegido(valor as CodigoServicio);
              reiniciarHora();
            }}
            opciones={servicios.datos.map((s) => ({
              valor: s.codigo,
              etiqueta: s.nombre,
              contenido: (
                <span className="servicio">
                  <span className="servicio__nombre">{s.nombre}</span>
                  <span className="servicio__duracion">
                    <IconoReloj /> {duracion(s.duracionMinutos)}
                  </span>
                  <span className="servicio__marca" aria-hidden="true" />
                </span>
              ),
            }))}
          />
        )}

        <div className="disponibilidad">
          <GrupoOpciones
            leyenda="Disponibilidad"
            nombre="dia"
            variante="fila"
            seleccionada={fecha}
            alElegir={(valor) => {
              setFecha(valor);
              reiniciarHora();
            }}
            opciones={dias.map((dia) => ({
              valor: dia,
              etiqueta: `${nombreDia(dia)} ${fechaCorta(dia)}`,
              contenido: (
                <span className="dia">
                  <span className="dia__nombre">{nombreDia(dia)}</span>
                  <span className="dia__numero">{numeroDia(dia)}</span>
                </span>
              ),
            }))}
          />

          {faltante ? (
            <p className="texto-tenue">{faltante}</p>
          ) : horarios.cargando ? (
            <p className="texto-tenue" role="status">
              Buscando horarios libres…
            </p>
          ) : horarios.error ? (
            <p className="aviso aviso--error" role="alert">
              {horarios.error}
            </p>
          ) : franjas.length === 0 ? (
            <p className="texto-tenue">No quedan horarios libres este día para este servicio. Prueba con otro día.</p>
          ) : (
            <GrupoOpciones
              leyenda="Hora"
              nombre="hora"
              variante="rejilla"
              seleccionada={horaElegida}
              alElegir={setHoraElegida}
              opciones={franjas.map((franja) => ({
                valor: hora(franja.horaInicio),
                contenido: hora(franja.horaInicio),
              }))}
            />
          )}
        </div>

        {aviso && (
          <p className="aviso aviso--error" role="alert">
            {aviso}
          </p>
        )}

        <div className="agendar__resumen">
          <div className="agendar__detalle" aria-live="polite">
            {barbero && servicio && horaElegida ? (
              <>
                <span className="agendar__servicio">
                  {servicio.nombre} · {duracion(servicio.duracionMinutos)} con {barbero.nombre}
                </span>
                <span className="agendar__cuando">
                  {nombreDia(fecha)} {fechaCorta(fecha)}, {horaElegida}
                </span>
              </>
            ) : (
              <span className="agendar__servicio">Elige barbero, servicio, día y hora.</span>
            )}
          </div>
          <button type="submit" className="boton boton--principal" disabled={!horaElegida || enviando}>
            {enviando ? 'Agendando…' : 'Agendar cita'}
          </button>
        </div>
      </form>
    </section>
  );
}
