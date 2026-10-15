import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

/** HU-22 / HU-23 / HU-24: Iniciar sesion (dueno, barbero, cliente). Flujo principal. */
export default function Login() {
  const [correo, setCorreo] = useState("dueno@barberia.com");
  const [password, setPassword] = useState("1234");
  const [error, setError] = useState(null);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const usuario = await api.login(correo, password);
      login(usuario);
      if (usuario.rol === "DUENO") navigate("/dueno");
      else if (usuario.rol === "BARBERO") navigate("/barbero");
      else navigate("/cliente");
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="card">
      <h1>Barberia</h1>
      <p className="subtitle">Inicia sesion con tu correo y contrasena</p>
      <form onSubmit={handleSubmit}>
        <label>Correo</label>
        <input value={correo} onChange={(e) => setCorreo(e.target.value)} />
        <label>Contrasena</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <button type="submit">Iniciar sesion</button>
        {error && <p className="error">{error}</p>}
      </form>
      <p className="hint">
        Demo: dueno@barberia.com / carlos@barberia.com / andres.gomez@correo.com — clave 1234
      </p>
    </div>
  );
}
