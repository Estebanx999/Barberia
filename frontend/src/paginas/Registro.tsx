import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ErrorApi } from '../api/cliente';
import type { DatosRegistro } from '../api/tipos';
import { Campo } from '../componentes/Campo';
import { PortadaAcceso } from '../componentes/PortadaAcceso';
import { INICIO_POR_ROL } from '../rutas/rutasPorRol';
import { useSesion } from '../sesion/useSesion';

type Errores = Partial<Record<keyof DatosRegistro, string>>;

const CORREO_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const LONGITUD_MINIMA = 8;

/** Mismas reglas que el backend, para avisar antes de enviar. */
export function validarRegistro(datos: DatosRegistro): Errores {
  const errores: Errores = {};
  if (!datos.nombre.trim()) errores.nombre = 'Escribe tu nombre.';
  if (!CORREO_VALIDO.test(datos.correo.trim())) errores.correo = 'Escribe un correo válido, por ejemplo nombre@correo.com.';
  if (!datos.telefono.trim()) errores.telefono = 'Escribe un teléfono para avisarte de tu cita.';
  if (datos.contrasena.length < LONGITUD_MINIMA) errores.contrasena = `Usa al menos ${LONGITUD_MINIMA} caracteres.`;
  if (datos.contrasena.length > 72) errores.contrasena = 'Usa máximo 72 caracteres.';
  return errores;
}

/** HU-21: registro de cliente. Al crear la cuenta, la sesión queda iniciada. */
export function Registro() {
  const { registrarse } = useSesion();
  const navegar = useNavigate();
  const [datos, setDatos] = useState<DatosRegistro>({ nombre: '', correo: '', telefono: '', contrasena: '' });
  const [errores, setErrores] = useState<Errores>({});
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const cambiar = (campo: keyof DatosRegistro) => (valor: string) => setDatos((previos) => ({ ...previos, [campo]: valor }));

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const encontrados = validarRegistro(datos);
    setErrores(encontrados);
    setError(null);
    if (Object.keys(encontrados).length > 0) {
      return;
    }
    setEnviando(true);
    try {
      const usuario = await registrarse({ ...datos, nombre: datos.nombre.trim(), correo: datos.correo.trim() });
      navegar(INICIO_POR_ROL[usuario.rol], { replace: true });
    } catch (causa) {
      if (causa instanceof ErrorApi && causa.estado === 409) {
        setErrores({ correo: 'Ya hay una cuenta con este correo. Inicia sesión con ella.' });
      } else {
        setError(causa instanceof ErrorApi ? causa.message : 'No se pudo crear la cuenta. Intenta de nuevo.');
      }
      setEnviando(false);
    }
  }

  return (
    <PortadaAcceso titulo="Crea tu cuenta">
      <form className="formulario" onSubmit={enviar} noValidate>
        {error && (
          <p className="aviso aviso--error" role="alert">
            {error}
          </p>
        )}
        <Campo
          etiqueta="Nombre"
          autoComplete="name"
          value={datos.nombre}
          onChange={(e) => cambiar('nombre')(e.target.value)}
          error={errores.nombre}
        />
        <Campo
          etiqueta="Correo"
          type="email"
          autoComplete="email"
          value={datos.correo}
          onChange={(e) => cambiar('correo')(e.target.value)}
          error={errores.correo}
        />
        <Campo
          etiqueta="Teléfono"
          type="tel"
          autoComplete="tel"
          value={datos.telefono}
          onChange={(e) => cambiar('telefono')(e.target.value)}
          error={errores.telefono}
        />
        <Campo
          etiqueta="Contraseña"
          type="password"
          autoComplete="new-password"
          value={datos.contrasena}
          onChange={(e) => cambiar('contrasena')(e.target.value)}
          error={errores.contrasena}
          ayuda={`Mínimo ${LONGITUD_MINIMA} caracteres.`}
        />
        <button type="submit" className="boton boton--principal" disabled={enviando}>
          {enviando ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>
      </form>
      <p className="tarjeta-acceso__pie">
        ¿Ya tienes cuenta? <Link to="/ingresar">Inicia sesión</Link>
      </p>
    </PortadaAcceso>
  );
}
