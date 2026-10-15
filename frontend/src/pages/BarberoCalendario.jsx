import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

/** HU-05 (Ver calendario propio) + HU-12/HU-13 (Registrar corte + comision) — flujo principal. */
export default function BarberoCalendario() {
  const { usuario, logout } = useAuth();
  const [fecha] = useState(new Date().toISOString().slice(0, 10));
  const [citas, setCitas] = useState([]);
  const [valorCobrado, setValorCobrado] = useState(15000);
  const [tieneCitaPrevia, setTieneCitaPrevia] = useState(true);
  const [clienteId, setClienteId] = useState(4); // demo: Andres Gomez (seed)
  const [registro, setRegistro] = useState(null);
  const [error, setError] = useState(null);

  const cargarCitas = () => {
    api.citasDelDia(usuario.id, fecha).then(setCitas);
  };

  useEffect(() => {
    cargarCitas();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleRegistrarCorte = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const corte = await api.registrarCorte({
        barberoId: usuario.id,
        clienteId: Number(clienteId),
        valorCobrado: Number(valorCobrado),
        tieneCitaPrevia,
      });
      setRegistro(corte);
      cargarCitas();
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

      <h2>Citas de hoy ({fecha})</h2>
      <ul className="list">
        {citas.map((c) => (
          <li key={c.id}>
            {c.horaInicio} - {c.horaFin} · {c.cliente?.nombre} · {c.estado}
          </li>
        ))}
        {citas.length === 0 && <li>Sin citas registradas hoy.</li>}
      </ul>

      <h2>Registrar servicio</h2>
      <form onSubmit={handleRegistrarCorte}>
        <label>Cliente ID</label>
        <input type="number" value={clienteId} onChange={(e) => setClienteId(e.target.value)} />
        <label>Valor cobrado</label>
        <input type="number" value={valorCobrado} onChange={(e) => setValorCobrado(e.target.value)} />
        <label>
          <input
            type="checkbox"
            checked={tieneCitaPrevia}
            onChange={(e) => setTieneCitaPrevia(e.target.checked)}
          />
          Tenia cita previa (desmarcar si es walk-in)
        </label>
        <button type="submit">Registrar y calcular comision</button>
      </form>
      {error && <p className="error">{error}</p>}
      {registro && (
        <p className="success">
          Corte #{registro.id} registrado. Comision barbero: ${registro.comision?.montoBarbero} ·
          Comision dueno: ${registro.comision?.montoDueno}
        </p>
      )}
    </div>
  );
}
