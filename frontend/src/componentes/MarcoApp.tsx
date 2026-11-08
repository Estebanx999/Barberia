import type { ComponentType } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { NAVEGACION, NOMBRE_ROL, type IconoSeccion } from '../rutas/rutasPorRol';
import { useSesion } from '../sesion/useSesion';
import { Avatar } from './Avatar';
import {
  IconoCalendario,
  IconoGrafica,
  IconoInicio,
  IconoMas,
  IconoMonedas,
  IconoRecibo,
  IconoSalir,
} from './Iconos';
import { Marca } from './Marca';

const ICONOS: Record<IconoSeccion, ComponentType<{ className?: string }>> = {
  inicio: IconoInicio,
  calendario: IconoCalendario,
  mas: IconoMas,
  monedas: IconoMonedas,
  recibo: IconoRecibo,
  grafica: IconoGrafica,
};

/**
 * Marco común de las páginas con sesión: encabezado, menú del rol y usuario.
 * En pantallas angostas el menú pasa a una barra inferior, como en la app móvil.
 */
export function MarcoApp() {
  const { usuario, cerrarSesion } = useSesion();
  const secciones = usuario ? NAVEGACION[usuario.rol] : [];

  return (
    <div
      className={[
        'marco',
        secciones.length > 0 ? 'marco--con-menu' : '',
        secciones.length > 3 ? 'marco--muchas-secciones' : '',
      ]
        .filter(Boolean)
        .join(' ')}
    >
      <header className="encabezado">
        <div className="encabezado__contenido">
          <Marca tamano="pequena" />
          {secciones.length > 0 && (
            <nav className="menu" aria-label="Secciones">
              {secciones.map((seccion) => {
                const Icono = ICONOS[seccion.icono];
                return (
                  <NavLink key={seccion.ruta} to={seccion.ruta} end className="menu__enlace">
                    <Icono className="menu__icono" />
                    <span>{seccion.texto}</span>
                  </NavLink>
                );
              })}
            </nav>
          )}
          {usuario && (
            <div className="encabezado__usuario">
              <Avatar nombre={usuario.nombre} tamano="pequeno" />
              <span className="encabezado__nombre">
                {usuario.nombre}
                <span className="encabezado__rol">{NOMBRE_ROL[usuario.rol]}</span>
              </span>
              <button type="button" className="boton boton--fantasma" onClick={cerrarSesion}>
                <IconoSalir />
                <span>Cerrar sesión</span>
              </button>
            </div>
          )}
        </div>
      </header>
      <main className="contenido">
        <Outlet />
      </main>
    </div>
  );
}
