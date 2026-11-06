/** Nombre del negocio con la tipografía condensada de letrero. */
export function Marca({ tamano = 'grande' }: { tamano?: 'grande' | 'pequena' }) {
  return <span className={`marca marca--${tamano}`}>Barbería</span>;
}
