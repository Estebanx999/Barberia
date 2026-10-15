import { useState } from "react";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

/** HU-14 (Registrar gasto) + HU-15/HU-16 (Ingresos/Utilidad neta por periodo) — flujo principal. */
export default function DuenoDashboard() {
  const { usuario, logout } = useAuth();
  const [monto, setMonto] = useState(500000);
  const [categoria, setCategoria] = useState("Arriendo");
  const [descripcion, setDescripcion] = useState("Arriendo local");
  const [gastoOk, setGastoOk] = useState(null);

  const [desde, setDesde] = useState(new Date(new Date().setDate(1)).toISOString().slice(0, 10));
  const [hasta, setHasta] = useState(new Date().toISOString().slice(0, 10));
  const [reporte, setReporte] = useState(null);
  const [error, setError] = useState(null);

  const handleGasto = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const gasto = await api.registrarGasto({
        duenoId: usuario.id,
        monto: Number(monto),
        categoria,
        descripcion,
      });
      setGastoOk(gasto);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleReporte = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const r = await api.reporte(desde, hasta);
      setReporte(r);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="card">
      <header>
        <h1>Hola, {usuario.nombre}</h1>
        <button className="link" onClick={logout}>Cerrar sesion</button>
      </header>

      <h2>Registrar gasto operativo</h2>
      <form onSubmit={handleGasto}>
        <label>Monto</label>
        <input type="number" value={monto} onChange={(e) => setMonto(e.target.value)} />
        <label>Categoria</label>
        <input value={categoria} onChange={(e) => setCategoria(e.target.value)} />
        <label>Descripcion</label>
        <input value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />
        <button type="submit">Guardar gasto</button>
      </form>
      {gastoOk && <p className="success">Gasto #{gastoOk.id} registrado.</p>}

      <h2>Reporte financiero (utilidad neta)</h2>
      <form onSubmit={handleReporte}>
        <label>Desde</label>
        <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} />
        <label>Hasta</label>
        <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} />
        <button type="submit">Ver reporte</button>
      </form>
      {error && <p className="error">{error}</p>}
      {reporte && (
        <div className="report">
          <p>Ingresos brutos: ${reporte.ingresosBrutos}</p>
          <p>Comisiones pagadas a barberos: ${reporte.comisionesPagadas}</p>
          <p>Gastos operativos: ${reporte.gastosOperativos}</p>
          <p><strong>Utilidad neta: ${reporte.utilidadNeta}</strong></p>
        </div>
      )}
    </div>
  );
}
