import { useCallback, useEffect, useState } from 'react';
import { ErrorApi } from '../api/cliente';

interface Carga<T> {
  datos: T | null;
  error: string | null;
  cargando: boolean;
  recargar: () => void;
}

/**
 * Carga datos al montar y cada vez que cambian las dependencias. Descarta las
 * respuestas que llegan tarde, para que no se mezclen dos consultas.
 * Si `cargar` es null no se consulta nada (por ejemplo, falta elegir algo).
 */
export function useCarga<T>(cargar: (() => Promise<T>) | null, dependencias: unknown[]): Carga<T> {
  const [datos, setDatos] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [cargando, setCargando] = useState(cargar !== null);
  const [version, setVersion] = useState(0);

  useEffect(() => {
    if (cargar === null) {
      setDatos(null);
      setCargando(false);
      return;
    }
    let vigente = true;
    setCargando(true);
    setError(null);
    cargar()
      .then((resultado) => vigente && setDatos(resultado))
      .catch((causa: unknown) => {
        if (vigente) {
          setDatos(null);
          setError(causa instanceof ErrorApi ? causa.message : 'No se pudo cargar la información.');
        }
      })
      .finally(() => vigente && setCargando(false));
    return () => {
      vigente = false;
    };
    // Las dependencias las define quien llama, como en useEffect.
  }, [...dependencias, version]);

  const recargar = useCallback(() => setVersion((v) => v + 1), []);
  return { datos, error, cargando, recargar };
}
