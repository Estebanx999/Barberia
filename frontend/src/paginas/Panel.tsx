import type { Rol } from '../api/tipos';
import { useSesion } from '../sesion/useSesion';

const DESCRIPCION: Record<Rol, string> = {
  CLIENTE: 'Desde aquí agendas tus citas y consultas las que tienes.',
  BARBERO: 'Desde aquí ves tu agenda del día, atiendes tus citas y consultas tus comisiones.',
  DUENO: 'Desde aquí ves la operación del día, registras los gastos y consultas los reportes del negocio.',
};

/** Panel de inicio de cada rol. Sus secciones se agregan en los siguientes módulos. */
export function Panel() {
  const { usuario } = useSesion();
  if (!usuario) {
    return null;
  }
  return (
    <section className="panel">
      <h1>Hola, {usuario.nombre.split(' ')[0]}</h1>
      <p>{DESCRIPCION[usuario.rol]}</p>
    </section>
  );
}
