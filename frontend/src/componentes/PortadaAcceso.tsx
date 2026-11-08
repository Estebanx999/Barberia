import type { ReactNode } from 'react';
import { Marca } from './Marca';

/** Diseño compartido por las páginas de ingreso y registro. */
export function PortadaAcceso({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <div className="portada">
      <section className="portada__marca">
        <Marca />
        <p className="portada__lema">Gestión de citas y comisiones</p>
      </section>
      <section className="tarjeta-acceso" aria-labelledby="titulo-acceso">
        <h1 id="titulo-acceso">{titulo}</h1>
        {children}
      </section>
    </div>
  );
}
