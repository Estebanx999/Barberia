import { Link } from 'react-router-dom';
import { Marca } from '../componentes/Marca';

/** HU-26: el rol del usuario no tiene acceso a la sección pedida. */
export function SinAcceso() {
  return (
    <div className="mensaje">
      <Marca tamano="pequena" />
      <h1>Esta sección no está disponible para tu cuenta</h1>
      <p>Cada rol ve solo lo que le corresponde.</p>
      <Link to="/" className="boton boton--principal">
        Volver a mi panel
      </Link>
    </div>
  );
}

export function NoEncontrada() {
  return (
    <div className="mensaje">
      <Marca tamano="pequena" />
      <h1>Esta página no existe</h1>
      <p>Revisa la dirección o vuelve a tu panel.</p>
      <Link to="/" className="boton boton--principal">
        Volver a mi panel
      </Link>
    </div>
  );
}
