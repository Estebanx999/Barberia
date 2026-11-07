import { useEffect, useState, type FormEvent } from 'react';
import { buscarClientes, registrarCorteSinCita } from '../../api/atencion';
import { ErrorApi } from '../../api/cliente';
import type { ClienteResumen, Corte } from '../../api/tipos';
import { Campo } from '../../componentes/Campo';
import { CamposCobro, type ErroresCobro, type ValoresCobro } from '../../componentes/CamposCobro';
import { GrupoOpciones } from '../../componentes/GrupoOpciones';
import { useSesion } from '../../sesion/useSesion';
import { pesos, parteDe } from '../../utilidades/dinero';
import { validarCobro } from './validarCobro';

type TipoCliente = 'registrado' | 'nuevo';
const ESPERA_BUSQUEDA_MS = 300;

/** HU-28: corte a un cliente que llegó sin cita. El cliente se busca o se registra en el momento. */
export function RegistrarCorteSinCita() {
  const { usuario } = useSesion();
  const [tipo, setTipo] = useState<TipoCliente>('registrado');
  const [busqueda, setBusqueda] = useState('');
  const [resultados, setResultados] = useState<ClienteResumen[] | null>(null);
  const [clienteId, setClienteId] = useState<number | null>(null);
  const [nuevo, setNuevo] = useState({ nombre: '', telefono: '' });
  const [valores, setValores] = useState<ValoresCobro>({ servicio: '', valor: '' });
  const [errores, setErrores] = useState<ErroresCobro & { cliente?: string }>({});
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [registrado, setRegistrado] = useState<Corte | null>(null);

  // Busca mientras el barbero escribe, esperando a que haga una pausa.
  useEffect(() => {
    const texto = busqueda.trim();
    if (tipo !== 'registrado' || texto.length < 2) {
      setResultados(null);
      return;
    }
    let vigente = true;
    const temporizador = setTimeout(() => {
      buscarClientes(texto)
        .then((encontrados) => vigente && setResultados(encontrados))
        .catch(() => vigente && setResultados([]));
    }, ESPERA_BUSQUEDA_MS);
    return () => {
      vigente = false;
      clearTimeout(temporizador);
    };
  }, [busqueda, tipo]);

  function reiniciar() {
    setRegistrado(null);
    setBusqueda('');
    setClienteId(null);
    setNuevo({ nombre: '', telefono: '' });
    setValores({ servicio: '', valor: '' });
    setErrores({});
  }

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError(null);
    const resultado = validarCobro(valores);
    const erroresCliente =
      tipo === 'registrado' && clienteId === null
        ? 'Busca y elige al cliente, o regístralo como cliente nuevo.'
        : tipo === 'nuevo' && !nuevo.nombre.trim()
          ? 'Escribe el nombre del cliente.'
          : undefined;
    if ('errores' in resultado || erroresCliente) {
      setErrores({ ...('errores' in resultado ? resultado.errores : {}), cliente: erroresCliente });
      return;
    }
    setErrores({});
    setEnviando(true);
    try {
      const cliente =
        tipo === 'registrado'
          ? { clienteId: clienteId! }
          : { clienteNombre: nuevo.nombre.trim(), clienteTelefono: nuevo.telefono.trim() || undefined };
      setRegistrado(await registrarCorteSinCita({ ...resultado.cobro, ...cliente }));
    } catch (causa) {
      setError(causa instanceof ErrorApi ? causa.message : 'No se pudo registrar el corte. Intenta de nuevo.');
    } finally {
      setEnviando(false);
    }
  }

  if (registrado) {
    return (
      <section className="pagina">
        <h1>Corte registrado</h1>
        <div className="resultado" role="status">
          <p>
            {registrado.servicio} a {registrado.clienteNombre ?? 'el cliente'}, sin cita previa.
          </p>
          <dl className="cifras">
            <div>
              <dt>Valor cobrado</dt>
              <dd>{pesos(registrado.valorCobrado)}</dd>
            </div>
            <div>
              <dt>Tu parte</dt>
              <dd>{pesos(parteDe(usuario!.rol, registrado.comision))}</dd>
            </div>
          </dl>
        </div>
        <button type="button" className="boton boton--principal" onClick={reiniciar}>
          Registrar otro corte
        </button>
      </section>
    );
  }

  return (
    <section className="pagina">
      <h1>Registrar corte sin cita</h1>
      <form className="agendar" onSubmit={enviar} noValidate>
        {error && (
          <p className="aviso aviso--error" role="alert">
            {error}
          </p>
        )}

        <GrupoOpciones
          leyenda="Cliente"
          nombre="tipo-cliente"
          variante="fila"
          seleccionada={tipo}
          alElegir={(valor) => {
            setTipo(valor as TipoCliente);
            setErrores({});
          }}
          opciones={[
            { valor: 'registrado', contenido: 'Ya está registrado' },
            { valor: 'nuevo', contenido: 'Es nuevo' },
          ]}
        />

        {tipo === 'registrado' ? (
          <div className="buscador">
            <Campo
              etiqueta="Buscar por nombre o teléfono"
              type="search"
              value={busqueda}
              onChange={(e) => {
                setBusqueda(e.target.value);
                setClienteId(null);
              }}
              error={errores.cliente}
              ayuda="Escribe al menos 2 letras o números."
            />
            {resultados && resultados.length === 0 && (
              <p className="texto-tenue">No hay clientes con ese nombre o teléfono. Regístralo como cliente nuevo.</p>
            )}
            {resultados && resultados.length > 0 && (
              <GrupoOpciones
                leyenda="Resultados"
                nombre="cliente"
                seleccionada={clienteId === null ? null : String(clienteId)}
                alElegir={(valor) => setClienteId(Number(valor))}
                opciones={resultados.map((c) => ({
                  valor: String(c.id),
                  etiqueta: c.nombre,
                  contenido: (
                    <span className="resultado-cliente">
                      <span>{c.nombre}</span>
                      {c.telefono && <span className="texto-tenue">{c.telefono}</span>}
                    </span>
                  ),
                }))}
              />
            )}
          </div>
        ) : (
          <div className="campos-cobro">
            <Campo
              etiqueta="Nombre del cliente"
              value={nuevo.nombre}
              onChange={(e) => setNuevo({ ...nuevo, nombre: e.target.value })}
              error={errores.cliente}
            />
            <Campo
              etiqueta="Teléfono (opcional)"
              type="tel"
              value={nuevo.telefono}
              onChange={(e) => setNuevo({ ...nuevo, telefono: e.target.value })}
            />
          </div>
        )}

        <fieldset className="grupo">
          <legend className="grupo__leyenda">Cobro</legend>
          <CamposCobro valores={valores} errores={errores} alCambiar={setValores} />
        </fieldset>

        <div>
          <button type="submit" className="boton boton--principal" disabled={enviando}>
            {enviando ? 'Registrando…' : 'Registrar corte'}
          </button>
        </div>
      </form>
    </section>
  );
}
