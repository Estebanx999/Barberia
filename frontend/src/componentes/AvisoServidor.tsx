import { useEffect, useState } from 'react';
import { EVENTO_SERVIDOR_INICIANDO } from '../api/cliente';

/** Aviso mientras el servidor se inicia, para que nadie repita la acción ni cierre la página. */
export function AvisoServidor() {
  const [iniciando, setIniciando] = useState(false);

  useEffect(() => {
    const alCambiar = (evento: Event) => setIniciando((evento as CustomEvent<boolean>).detail);
    window.addEventListener(EVENTO_SERVIDOR_INICIANDO, alCambiar);
    return () => window.removeEventListener(EVENTO_SERVIDOR_INICIANDO, alCambiar);
  }, []);

  if (!iniciando) {
    return null;
  }
  return (
    <div className="aviso-servidor" role="status">
      <span className="aviso-servidor__indicador" aria-hidden="true" />
      Iniciando el servidor… La primera vez puede tardar hasta 3 minutos. No cierres la página.
    </div>
  );
}
