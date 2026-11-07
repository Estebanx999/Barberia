import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ErrorApi } from '../api/cliente';
import { Campo } from '../componentes/Campo';
import { PortadaAcceso } from '../componentes/PortadaAcceso';
import { INICIO_POR_ROL } from '../rutas/rutasPorRol';
import { useSesion } from '../sesion/useSesion';

/** HU-22, HU-23, HU-24: un solo formulario; cada rol llega a su panel. */
export function InicioSesion() {
  const { iniciarSesion } = useSesion();
  const navegar = useNavigate();
  const ubicacion = useLocation();
  const [correo, setCorreo] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const desde = (ubicacion.state as { desde?: string } | null)?.desde;

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (!correo.trim() || !contrasena) {
      setError('Escribe tu correo y tu contraseña.');
      return;
    }
    setError(null);
    setEnviando(true);
    try {
      const usuario = await iniciarSesion(correo.trim(), contrasena);
      navegar(desde ?? INICIO_POR_ROL[usuario.rol], { replace: true });
    } catch (causa) {
      setError(causa instanceof ErrorApi ? causa.message : 'No se pudo iniciar sesión. Intenta de nuevo.');
      setEnviando(false);
    }
  }

  return (
    <PortadaAcceso titulo="Iniciar sesión">
      <form className="formulario" onSubmit={enviar} noValidate>
        {error && (
          <p className="aviso aviso--error" role="alert">
            {error}
          </p>
        )}
        <Campo
          etiqueta="Correo"
          type="email"
          autoComplete="email"
          value={correo}
          onChange={(e) => setCorreo(e.target.value)}
        />
        <Campo
          etiqueta="Contraseña"
          type="password"
          autoComplete="current-password"
          value={contrasena}
          onChange={(e) => setContrasena(e.target.value)}
        />
        <button type="submit" className="boton boton--principal" disabled={enviando}>
          {enviando ? 'Iniciando sesión…' : 'Iniciar sesión'}
        </button>
      </form>
      <p className="tarjeta-acceso__pie">
        ¿Vienes por primera vez? <Link to="/registro">Crea tu cuenta de cliente</Link>
      </p>
    </PortadaAcceso>
  );
}
