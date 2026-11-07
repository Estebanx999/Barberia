import { useState, type FormEvent } from 'react';
import { ErrorApi } from '../../api/cliente';
import { registrarGasto, reporteGastos } from '../../api/negocio';
import { Campo } from '../../componentes/Campo';
import { pesos, leerValor } from '../../utilidades/dinero';
import { hoyISO } from '../../utilidades/fechas';
import { periodoPredefinido } from '../../utilidades/periodos';
import { useCarga } from '../../utilidades/useCarga';

type Errores = Partial<Record<'concepto' | 'fecha' | 'valor', string>>;

/** HU-14: el dueño registra los gastos operativos; entran en los reportes de su periodo. */
export function Gastos() {
  const [concepto, setConcepto] = useState('');
  const [fecha, setFecha] = useState(hoyISO());
  const [valor, setValor] = useState('');
  const [errores, setErrores] = useState<Errores>({});
  const [aviso, setAviso] = useState<{ tipo: 'exito' | 'error'; texto: string } | null>(null);
  const [enviando, setEnviando] = useState(false);

  const mes = periodoPredefinido('mes');
  const delMes = useCarga(() => reporteGastos(mes), []);

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const monto = leerValor(valor);
    const encontrados: Errores = {};
    if (!concepto.trim()) encontrados.concepto = 'Escribe en qué se gastó, por ejemplo Insumos o Arriendo.';
    if (!fecha) encontrados.fecha = 'Elige la fecha del gasto.';
    if (monto === null) encontrados.valor = 'Escribe el valor del gasto, por ejemplo 50000.';
    setErrores(encontrados);
    setAviso(null);
    if (Object.keys(encontrados).length > 0 || monto === null) {
      return;
    }
    setEnviando(true);
    try {
      const gasto = await registrarGasto({ concepto: concepto.trim(), fecha, valor: monto });
      setAviso({ tipo: 'exito', texto: `Gasto registrado: ${gasto.concepto}, ${pesos(gasto.valor)}.` });
      setConcepto('');
      setValor('');
      delMes.recargar();
    } catch (causa) {
      setAviso({
        tipo: 'error',
        texto: causa instanceof ErrorApi ? causa.message : 'No se pudo registrar el gasto. Intenta de nuevo.',
      });
    } finally {
      setEnviando(false);
    }
  }

  return (
    <section className="pagina">
      <h1>Gastos</h1>

      <div className="dos-columnas">
        <form className="formulario tarjeta" onSubmit={enviar} noValidate aria-label="Registrar gasto">
          <h2>Registrar gasto</h2>
          {aviso && (
            <p className={`aviso aviso--${aviso.tipo}`} role={aviso.tipo === 'error' ? 'alert' : 'status'}>
              {aviso.texto}
            </p>
          )}
          <Campo etiqueta="Concepto" value={concepto} onChange={(e) => setConcepto(e.target.value)} error={errores.concepto} />
          <Campo
            etiqueta="Fecha"
            type="date"
            value={fecha}
            onChange={(e) => setFecha(e.target.value)}
            error={errores.fecha}
          />
          <Campo
            etiqueta="Valor"
            inputMode="numeric"
            value={valor}
            onChange={(e) => setValor(e.target.value)}
            error={errores.valor}
            ayuda="En pesos, sin decimales."
          />
          <button type="submit" className="boton boton--principal" disabled={enviando}>
            {enviando ? 'Registrando…' : 'Registrar gasto'}
          </button>
        </form>

        <section className="bloque" aria-labelledby="gastos-mes">
          <h2 id="gastos-mes">Gastos de este mes</h2>
          {delMes.error && (
            <p className="aviso aviso--error" role="alert">
              {delMes.error}
            </p>
          )}
          {delMes.datos && delMes.datos.detalle.length === 0 && (
            <p className="texto-tenue">Aún no hay gastos registrados este mes.</p>
          )}
          {delMes.datos && delMes.datos.detalle.length > 0 && <TablaConceptos reporte={delMes.datos} />}
        </section>
      </div>
    </section>
  );
}

export function TablaConceptos({
  reporte,
}: {
  reporte: { totalGastos: number; detalle: { concepto: string; cantidad: number; total: number }[] };
}) {
  return (
    <table className="tabla tabla--simple">
      <caption className="solo-lectores">Gastos por concepto</caption>
      <thead>
        <tr>
          <th scope="col">Concepto</th>
          <th scope="col" className="numero">
            Registros
          </th>
          <th scope="col" className="numero">
            Total
          </th>
        </tr>
      </thead>
      <tbody>
        {reporte.detalle.map((fila) => (
          <tr key={fila.concepto}>
            <td>{fila.concepto}</td>
            <td className="numero">{fila.cantidad}</td>
            <td className="numero">{pesos(fila.total)}</td>
          </tr>
        ))}
      </tbody>
      <tfoot>
        <tr>
          <th scope="row" colSpan={2}>
            Total
          </th>
          <td className="numero">{pesos(reporte.totalGastos)}</td>
        </tr>
      </tfoot>
    </table>
  );
}
