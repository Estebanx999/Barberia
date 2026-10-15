import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

/** HU-01 (Agendar cita) + HU-02 (consultar disponibilidad) + HU-07 (validar cruce) — flujo principal. */
export default function ClienteAgendarCita() {
  const { usuario, logout } = useAuth();
  const [barberos, setBarberos] = useState([]);
  const [barberoId, setBarberoId] = useState("");
  const [fecha, setFecha] = useState(new Date().toISOString().slice(0, 10));
  const [horaInicio, setHoraInicio] = useState("10:00");
  const [horaFin, setHoraFin] = useState("10:30");
  const [confirmacion, setConfirmacion] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.listarBarberos().then((data) => {
      setBarberos(data);
      if (data.length) setBarberoId(data[0].id);
    });
  }, []);

  const handleAgendar = async (e) => {
    e.preventDefault();
    setError(null);
    setConfirmacion(null);
    try {
      const cita = await api.agendarCita({
        clienteId: usuario.id,
        barberoId: Number(barberoId),
        fecha,
        horaInicio,
        horaFin,
      });
      setConfirmacion(cita);
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
      <h2>Agendar cita</h2>
      <form onSubmit={handleAgendar}>
        <label>Barbero</label>
        <select value={barberoId} onChange={(e) => setBarberoId(e.target.value)}>
          {barberos.map((b) => (
            <option key={b.id} value={b.id}>{b.nombre}</option>
          ))}
        </select>
        <label>Fecha</label>
        <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} />
        <label>Hora inicio</label>
        <input type="time" value={horaInicio} onChange={(e) => setHoraInicio(e.target.value)} />
        <label>Hora fin</label>
        <input type="time" value={horaFin} onChange={(e) => setHoraFin(e.target.value)} />
        <button type="submit">Confirmar cita</button>
      </form>
      {error && <p className="error">{error}</p>}
      {confirmacion && (
        <p className="success">
          Cita #{confirmacion.id} confirmada con {confirmacion.barbero?.nombre} el {confirmacion.fecha} a las {confirmacion.horaInicio}.
        </p>
      )}
    </div>
  );
}
