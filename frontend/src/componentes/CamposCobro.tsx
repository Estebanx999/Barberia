import { Campo } from './Campo';

export interface ValoresCobro {
  servicio: string;
  valor: string;
}

export type ErroresCobro = Partial<Record<keyof ValoresCobro, string>>;

/** Servicio y valor cobrado (HU-27, HU-28): los dos datos que pide el registro de un corte. */
export function CamposCobro({
  valores,
  errores,
  alCambiar,
}: {
  valores: ValoresCobro;
  errores: ErroresCobro;
  alCambiar: (valores: ValoresCobro) => void;
}) {
  return (
    <div className="campos-cobro">
      <Campo
        etiqueta="Servicio"
        value={valores.servicio}
        onChange={(e) => alCambiar({ ...valores, servicio: e.target.value })}
        error={errores.servicio}
        placeholder="Corte clásico"
      />
      <Campo
        etiqueta="Valor cobrado"
        inputMode="numeric"
        value={valores.valor}
        onChange={(e) => alCambiar({ ...valores, valor: e.target.value })}
        error={errores.valor}
        placeholder="30000"
        ayuda="En pesos, sin decimales."
      />
    </div>
  );
}
