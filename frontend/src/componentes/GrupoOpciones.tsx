import type { ReactNode } from 'react';

export interface Opcion {
  valor: string;
  contenido: ReactNode;
  /** Texto que leen los lectores de pantalla si el contenido es visual. */
  etiqueta?: string;
}

interface Props {
  leyenda: string;
  nombre: string;
  opciones: Opcion[];
  seleccionada: string | null;
  alElegir: (valor: string) => void;
  variante?: 'lista' | 'fila' | 'rejilla';
}

/** Grupo de opciones de selección única, con radios nativos para teclado y lectores de pantalla. */
export function GrupoOpciones({ leyenda, nombre, opciones, seleccionada, alElegir, variante = 'lista' }: Props) {
  return (
    <fieldset className="grupo">
      <legend className="grupo__leyenda">{leyenda}</legend>
      <div className={`opciones opciones--${variante}`}>
        {opciones.map((opcion) => (
          <label key={opcion.valor} className="opcion">
            <input
              type="radio"
              name={nombre}
              value={opcion.valor}
              checked={seleccionada === opcion.valor}
              onChange={() => alElegir(opcion.valor)}
              aria-label={opcion.etiqueta}
            />
            <span className="opcion__cara">{opcion.contenido}</span>
          </label>
        ))}
      </div>
    </fieldset>
  );
}
