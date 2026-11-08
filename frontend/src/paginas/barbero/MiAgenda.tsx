import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { calendarioBarbero, marcarAtendida, misCortes, registrarCobro } from '../../api/atencion';
import { ErrorApi } from '../../api/cliente';
import type { Cita, Corte } from '../../api/tipos';
import { CamposCobro, type ErroresCobro, type ValoresCobro } from '../../componentes/CamposCobro';
import { SelectorDia } from '../../componentes/SelectorDia';
import { INICIO_POR_ROL } from '../../rutas/rutasPorRol';
import { useSesion } from '../../sesion/useSesion';
import { pesos, parteDe } from '../../utilidades/dinero';
import { hora, hoyISO } from '../../utilidades/fechas';
import { useCarga } from '../../utilidades/useCarga';
import { validarCobro } from './validarCobro';

/**
 * HU-05: agenda del día del barbero, ordenada por hora.
 * HU-12: marcar la cita como atendida. HU-27: registrar el cobro de la cita atendida.
 */
export function MiAgenda() {
  const { usuario } = useSesion();
  const hoy = hoyISO();
  const [fecha, setFecha] = useState(hoy);
  const [aviso, setAviso] = useState<{ tipo: 'exito' | 'error'; texto: string } | null>(null);

  const citas = useCarga(() => calendarioBarbero(usuario!.id, fecha), [fecha]);
  // Los cortes del día dicen qué citas atendidas ya tienen su cobro registrado.
  const cortes = useCarga(() => misCortes(fecha, fecha), [fecha]);
  const cortePorCita = new Map<number, Corte>(
    (cortes.datos?.cortes ?? []).filter((c) => c.citaId !== null).map((c) => [c.citaId!, c]),
  );

  function cambiarDia(nueva: string) {
    setFecha(nueva);
    setAviso(null);
  }

  function actualizar(texto: string) {
    setAviso({ tipo: 'exito', texto });
    citas.recargar();
    cortes.recargar();
  }

  return (
    <section className="pagina">
      <div className="pagina__titulo">
        <h1>Mi agenda</h1>
        <Link to={`${INICIO_POR_ROL[usuario!.rol]}/sin-cita`} className="boton boton--dorado">
          + Registrar corte sin cita
        </Link>
      </div>

      <SelectorDia fecha={fecha} alCambiar={cambiarDia} />

      {citas.datos && cortes.datos && (
        <dl className="cifras" aria-label="Resumen del día">
          <div>
            <dt>Citas</dt>
            <dd>{citas.datos.length}</dd>
          </div>
          <div>
            <dt>Cortes cobrados</dt>
            <dd>{cortes.datos.cantidadCortes}</dd>
          </div>
          <div className="cifras__destacada">
            <dt>Tu parte</dt>
            <dd>{pesos(cortes.datos.cortes.reduce((total, c) => total + parteDe(usuario!.rol, c.comision), 0))}</dd>
          </div>
        </dl>
      )}

      {aviso && (
        <p className={`aviso aviso--${aviso.tipo}`} role={aviso.tipo === 'error' ? 'alert' : 'status'}>
          {aviso.texto}
        </p>
      )}
      {citas.error && (
        <p className="aviso aviso--error" role="alert">
          {citas.error}
        </p>
      )}
      {citas.cargando && !citas.datos && <p className="texto-tenue">Cargando tu agenda…</p>}

      {citas.datos && citas.datos.length === 0 && (
        <div className="vacio">
          <p>No tienes citas este día. Si llega alguien sin cita, regístralo en "Registrar corte sin cita".</p>
        </div>
      )}

      {citas.datos && citas.datos.length > 0 && (
        <ul className="citas">
          {citas.datos.map((cita) => (
            <FilaCita
              key={cita.id}
              cita={cita}
              corte={cortePorCita.get(cita.id)}
              sePuedeAtender={cita.fecha <= hoy}
              alAtender={() => actualizar(`Cita de las ${hora(cita.horaInicio)} marcada como atendida. Ya puedes registrar el cobro.`)}
              alCobrar={(corte) =>
                actualizar(
                  `Cobro registrado: ${pesos(corte.valorCobrado)}. Tu parte: ${pesos(parteDe(usuario!.rol, corte.comision))}.`,
                )
              }
              alFallar={(texto) => setAviso({ tipo: 'error', texto })}
            />
          ))}
        </ul>
      )}
    </section>
  );
}

interface PropsFila {
  cita: Cita;
  corte: Corte | undefined;
  sePuedeAtender: boolean;
  alAtender: () => void;
  alCobrar: (corte: Corte) => void;
  alFallar: (mensaje: string) => void;
}

function FilaCita({ cita, corte, sePuedeAtender, alAtender, alCobrar, alFallar }: PropsFila) {
  const [cobrando, setCobrando] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [valores, setValores] = useState<ValoresCobro>({ servicio: cita.servicioNombre, valor: '' });
  const [errores, setErrores] = useState<ErroresCobro>({});

  const mensajeDe = (causa: unknown) =>
    causa instanceof ErrorApi ? causa.message : 'No se pudo completar la operación. Intenta de nuevo.';

  async function atender() {
    setEnviando(true);
    try {
      await marcarAtendida(cita.id);
      alAtender();
    } catch (causa) {
      alFallar(mensajeDe(causa));
    } finally {
      setEnviando(false);
    }
  }

  async function cobrar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const resultado = validarCobro(valores);
    if ('errores' in resultado) {
      setErrores(resultado.errores);
      return;
    }
    setErrores({});
    setEnviando(true);
    try {
      const registrado = await registrarCobro(cita.id, resultado.cobro);
      setCobrando(false);
      alCobrar(registrado);
    } catch (causa) {
      alFallar(mensajeDe(causa));
    } finally {
      setEnviando(false);
    }
  }

  const cliente = cita.clienteNombre ?? 'Cliente';

  return (
    <li className="cita cita--agenda" aria-label={`${hora(cita.horaInicio)} ${cliente}`}>
      <span className="cita__hora-grande">{hora(cita.horaInicio)}</span>
      <span className="cita__detalle">
        <span className="cita__titulo">{cliente}</span>
        <span className="texto-tenue">
          {cita.servicioNombre} · hasta las {hora(cita.horaFin)}
        </span>
      </span>

      <span className="cita__acciones">
        {cita.estado === 'AGENDADA' && sePuedeAtender && (
          <button type="button" className="boton boton--principal" onClick={atender} disabled={enviando}>
            Marcar atendida
          </button>
        )}
        {cita.estado === 'AGENDADA' && !sePuedeAtender && <span className="estado estado--agendada">Agendada</span>}
        {cita.estado === 'ATENDIDA' && corte && (
          <span className="estado">Cobrado {pesos(corte.valorCobrado)}</span>
        )}
        {cita.estado === 'ATENDIDA' && !corte && !cobrando && (
          <button type="button" className="boton boton--principal" onClick={() => setCobrando(true)}>
            Registrar cobro
          </button>
        )}
      </span>

      {cobrando && (
        <form className="cita__cobro" onSubmit={cobrar} noValidate aria-label={`Cobro de la cita de ${cliente}`}>
          <CamposCobro valores={valores} errores={errores} alCambiar={setValores} />
          <div className="cita__cobro-botones">
            <button type="submit" className="boton boton--principal" disabled={enviando}>
              {enviando ? 'Guardando…' : 'Guardar cobro'}
            </button>
            <button type="button" className="boton boton--secundario" onClick={() => setCobrando(false)}>
              Cancelar
            </button>
          </div>
        </form>
      )}
    </li>
  );
}
