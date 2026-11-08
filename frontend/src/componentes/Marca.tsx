import { IconoTijeras } from './Iconos';

/** Sello y nombre del negocio. En la portada va grande y con el lema. */
export function Marca({ tamano = 'grande' }: { tamano?: 'grande' | 'pequena' }) {
  return (
    <span className={`marca marca--${tamano}`}>
      <span className="marca__sello" aria-hidden="true">
        <IconoTijeras />
      </span>
      <span className="marca__nombre">Barbería</span>
    </span>
  );
}
