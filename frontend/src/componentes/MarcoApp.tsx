import { NavLink, Outlet } from 'react-router-dom';
import { NAVEGACION, NOMBRE_ROL } from '../rutas/rutasPorRol';
import { useSesion } from '../sesion/useSesion';
import { Marca } from './Marca';

/** Marco común de las páginas con sesión: encabezado, menú del rol y usuario. */
export function MarcoApp() {
  const { usuario, cerrarSesion } = useSesion();
  const secciones = usuario ? NAVEGACION[usuario.rol] : [];

  return (
    <div className="marco">
      <header className="encabezado">
        <div className="poste" aria-hidden="true" />
        <div className="encabezado__contenido">
          <Marca tamano="pequena" />
          {secciones.length > 0 && (
            <nav className="menu" aria-label="Secciones">
              {secciones.map((seccion) => (
                <NavLink key={seccion.ruta} to={seccion.ruta} end className="menu__enlace">
                  {seccion.texto}
                </NavLink>
              ))}
            </nav>
          )}
          {usuario && (
            <div className="encabezado__usuario">
              <span>
                {usuario.nombre}
                <span className="encabezado__rol">{NOMBRE_ROL[usuario.rol]}</span>
              </span>
              <button type="button" className="boton boton--secundario" onClick={cerrarSesion}>
                Cerrar sesión
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
