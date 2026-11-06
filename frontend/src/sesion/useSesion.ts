import { useContext } from 'react';
import { SesionContext, type EstadoSesion } from './SesionContext';

export function useSesion(): EstadoSesion {
  const sesion = useContext(SesionContext);
  if (sesion === null) {
    throw new Error('useSesion debe usarse dentro de <SesionProvider>');
  }
  return sesion;
}
