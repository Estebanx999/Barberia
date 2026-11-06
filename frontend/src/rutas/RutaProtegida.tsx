import { Navigate, Outlet, useLocation } from 'react-router-dom';
import type { Rol } from '../api/tipos';
import { useSesion } from '../sesion/useSesion';
import { INICIO_POR_ROL } from './rutasPorRol';

/**
 * HU-26: solo deja pasar a los roles indicados. Es una ayuda de navegación;
 * la protección real la hace el backend en cada solicitud.
 */
export function RutaProtegida({ roles }: { roles: Rol[] }) {
  const { usuario, cargando } = useSesion();
  const ubicacion = useLocation();

  if (cargando) {
    return <p className="cargando" role="status">Cargando tu sesión…</p>;
  }
  if (usuario === null) {
    return <Navigate to="/ingresar" replace state={{ desde: ubicacion.pathname }} />;
  }
  if (!roles.includes(usuario.rol)) {
    return <Navigate to="/sin-acceso" replace />;
  }
  return <Outlet />;
}

/** Las páginas de ingreso y registro no se muestran a quien ya inició sesión. */
export function SoloInvitados() {
  const { usuario, cargando } = useSesion();
  if (cargando) {
    return <p className="cargando" role="status">Cargando tu sesión…</p>;
  }
  if (usuario !== null) {
    return <Navigate to={INICIO_POR_ROL[usuario.rol]} replace />;
  }
  return <Outlet />;
}

export function RedireccionInicial() {
  const { usuario, cargando } = useSesion();
  if (cargando) {
    return <p className="cargando" role="status">Cargando tu sesión…</p>;
  }
  return <Navigate to={usuario ? INICIO_POR_ROL[usuario.rol] : '/ingresar'} replace />;
}
