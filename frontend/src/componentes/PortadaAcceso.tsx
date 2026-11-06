import type { ReactNode } from 'react';
import { Marca } from './Marca';

/** Diseño compartido por las páginas de ingreso y registro. */
export function PortadaAcceso({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <div className="portada">
      <section className="portada__marca">
        <Marca />
        <p className="portada__lema">
          Agenda tu corte, atiende tu silla y lleva las cuentas del negocio en un solo lugar.
        </p>
      </section>
      <section className="tarjeta-acceso" aria-labelledby="titulo-acceso">
        <div className="poste" aria-hidden="true" />
        <div className="tarjeta-acceso__cuerpo">
          <h1 id="titulo-acceso">{titulo}</h1>
          {children}
        </div>
      </section>
    </div>
  );
}
