import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import * as api from '../api/autenticacion';
import { EVENTO_SESION_EXPIRADA } from '../api/cliente';
import type { DatosRegistro, Usuario } from '../api/tipos';
import { borrarToken, guardarToken, leerToken } from './almacen';

export interface EstadoSesion {
  usuario: Usuario | null;
  /** Verdadero mientras se valida un token guardado al abrir la app. */
  cargando: boolean;
  iniciarSesion: (correo: string, contrasena: string) => Promise<Usuario>;
  registrarse: (datos: DatosRegistro) => Promise<Usuario>;
  cerrarSesion: () => void;
}

export const SesionContext = createContext<EstadoSesion | null>(null);

export function SesionProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [cargando, setCargando] = useState(() => leerToken() !== null);

  // Si hay un token guardado, se confirma con el backend antes de mostrar la app.
  useEffect(() => {
    if (leerToken() === null) {
      return;
    }
    let vigente = true;
    api
      .obtenerUsuarioActual()
      .then((encontrado) => vigente && setUsuario(encontrado))
      .catch(() => borrarToken())
      .finally(() => vigente && setCargando(false));
    return () => {
      vigente = false;
    };
  }, []);

  useEffect(() => {
    const alExpirar = () => setUsuario(null);
    window.addEventListener(EVENTO_SESION_EXPIRADA, alExpirar);
    return () => window.removeEventListener(EVENTO_SESION_EXPIRADA, alExpirar);
  }, []);

  const iniciarSesion = useCallback(async (correo: string, contrasena: string) => {
    const sesion = await api.iniciarSesion(correo, contrasena);
    guardarToken(sesion.token);
    setUsuario(sesion.usuario);
    return sesion.usuario;
  }, []);

  /** HU-21: crea la cuenta y deja al cliente con la sesión iniciada. */
  const registrarse = useCallback(
    async (datos: DatosRegistro) => {
      await api.registrarCliente(datos);
      return iniciarSesion(datos.correo, datos.contrasena);
    },
    [iniciarSesion],
  );

  const cerrarSesion = useCallback(() => {
    borrarToken();
    setUsuario(null);
  }, []);

  const valor = useMemo(
    () => ({ usuario, cargando, iniciarSesion, registrarse, cerrarSesion }),
    [usuario, cargando, iniciarSesion, registrarse, cerrarSesion],
  );

  return <SesionContext.Provider value={valor}>{children}</SesionContext.Provider>;
}
