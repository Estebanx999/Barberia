import { Outlet } from 'react-router-dom';
import { NOMBRE_ROL } from '../rutas/rutasPorRol';
import { useSesion } from '../sesion/useSesion';
import { Marca } from './Marca';

/** Marco común de las páginas con sesión: encabezado con el usuario y su rol. */
export function MarcoApp() {
  const { usuario, cerrarSesion } = useSesion();

  return (
    <div className="marco">
      <header className="encabezado">
        <div className="poste" aria-hidden="true" />
        <div className="encabezado__contenido">
          <Marca tamano="pequena" />
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
