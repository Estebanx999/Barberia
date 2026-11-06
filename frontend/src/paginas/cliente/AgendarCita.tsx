import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { agendarCita, horariosDisponibles, horariosRestantes, listarBarberos } from '../../api/agenda';
import { ErrorApi } from '../../api/cliente';
import type { Franja } from '../../api/tipos';
import { GrupoOpciones } from '../../componentes/GrupoOpciones';
import { diaYMes, fechaLarga, hora, nombreDia, proximosDias } from '../../utilidades/fechas';
import { useCarga } from '../../utilidades/useCarga';

const DIAS_VISIBLES = 14;

/**
 * HU-01: agendar con el barbero de preferencia. HU-02: solo se ofrecen los
 * horarios libres. HU-07: si el horario se ocupó mientras tanto, el backend lo
 * rechaza y se muestran los horarios que siguen libres.
 */
export function AgendarCita() {
  const navegar = useNavigate();
  const [dias] = useState(() => proximosDias(DIAS_VISIBLES));
  const [barberoId, setBarberoId] = useState<number | null>(null);
  const [fecha, setFecha] = useState<string>(dias[0]!);
  const [horaElegida, setHoraElegida] = useState<string | null>(null);
  const [horariosTrasConflicto, setHorariosTrasConflicto] = useState<Franja[] | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const barberos = useCarga(listarBarberos, []);
  const horarios = useCarga(
    barberoId === null ? null : () => horariosDisponibles(barberoId, fecha),
    [barberoId, fecha],
  );
  const franjas = horariosTrasConflicto ?? horarios.datos ?? [];
  const barbero = barberos.datos?.find((b) => b.id === barberoId);

  function reiniciarHora() {
    setHoraElegida(null);
    setHorariosTrasConflicto(null);
    setAviso(null);
  }

  async function confirmar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (barberoId === null || horaElegida === null) {
      return;
    }
    setEnviando(true);
    setAviso(null);
    try {
      const cita = await agendarCita({ barberoId, fecha, horaInicio: horaElegida });
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

  return (
    <section className="pagina">
      <h1>Agendar cita</h1>

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
            leyenda="Barbero"
            nombre="barbero"
            variante="fila"
            seleccionada={barberoId === null ? null : String(barberoId)}
            alElegir={(valor) => {
              setBarberoId(Number(valor));
              reiniciarHora();
            }}
            opciones={barberos.datos.map((b) => ({ valor: String(b.id), contenido: b.nombre }))}
          />
        )}

        <GrupoOpciones
          leyenda="Día"
          nombre="dia"
          variante="fila"
          seleccionada={fecha}
          alElegir={(valor) => {
            setFecha(valor);
            reiniciarHora();
          }}
          opciones={dias.map((dia) => ({
            valor: dia,
            etiqueta: `${nombreDia(dia)} ${diaYMes(dia)}`,
            contenido: (
              <span className="dia">
                <span className="dia__nombre">{nombreDia(dia)}</span>
                <span className="dia__numero">{diaYMes(dia)}</span>
              </span>
            ),
          }))}
        />

        {barberoId === null ? (
          <p className="texto-tenue">Elige un barbero para ver sus horarios libres.</p>
        ) : horarios.cargando ? (
          <p className="texto-tenue" role="status">
            Buscando horarios libres…
          </p>
        ) : horarios.error ? (
          <p className="aviso aviso--error" role="alert">
            {horarios.error}
          </p>
        ) : franjas.length === 0 ? (
          <p className="texto-tenue">No quedan horarios libres este día. Prueba con otro día.</p>
        ) : (
          <GrupoOpciones
            leyenda="Hora"
            nombre="hora"
            variante="rejilla"
            seleccionada={horaElegida}
            alElegir={setHoraElegida}
            opciones={franjas.map((franja) => ({ valor: hora(franja.horaInicio), contenido: hora(franja.horaInicio) }))}
          />
        )}

        {aviso && (
          <p className="aviso aviso--error" role="alert">
            {aviso}
          </p>
        )}

        <div className="agendar__resumen">
          <p aria-live="polite">
            {barbero && horaElegida
              ? `Con ${barbero.nombre}, ${fechaLarga(fecha).toLowerCase()} a las ${horaElegida}.`
              : 'Elige barbero, día y hora.'}
          </p>
          <button type="submit" className="boton boton--principal" disabled={!horaElegida || enviando}>
            {enviando ? 'Agendando…' : 'Agendar cita'}
          </button>
        </div>
      </form>
    </section>
  );
}
