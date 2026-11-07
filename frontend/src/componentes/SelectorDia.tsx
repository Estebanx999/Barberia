import { fechaLarga, hoyISO, sumarDias } from '../utilidades/fechas';

/** Cambia el día que se está viendo: anterior, siguiente o volver a hoy. */
export function SelectorDia({ fecha, alCambiar }: { fecha: string; alCambiar: (fecha: string) => void }) {
  const hoy = hoyISO();
  return (
    <div className="selector-dia">
      <button type="button" className="boton boton--secundario" onClick={() => alCambiar(sumarDias(fecha, -1))}>
        Día anterior
      </button>
      <p className="selector-dia__fecha" aria-live="polite">
        {fechaLarga(fecha)}
      </p>
      <button type="button" className="boton boton--secundario" onClick={() => alCambiar(sumarDias(fecha, 1))}>
        Día siguiente
      </button>
      {fecha !== hoy && (
        <button type="button" className="boton boton--enlace" onClick={() => alCambiar(hoy)}>
          Volver a hoy
        </button>
      )}
    </div>
  );
}
