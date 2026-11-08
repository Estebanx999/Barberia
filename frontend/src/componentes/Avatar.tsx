import { useState } from 'react';

/** Iniciales de un nombre: "Carlos Ramírez" → "CR". */
export function iniciales(nombre: string): string {
  const partes = nombre.trim().split(/\s+/).filter(Boolean);
  const letras = partes.length > 1 ? [partes[0]!, partes[partes.length - 1]!] : partes;
  return letras.map((parte) => parte.charAt(0).toUpperCase()).join('');
}

/** Foto de la persona; si no tiene o no carga, sus iniciales. */
export function Avatar({
  nombre,
  fotoUrl,
  tamano = 'mediano',
}: {
  nombre: string;
  fotoUrl?: string | null;
  tamano?: 'pequeno' | 'mediano' | 'grande';
}) {
  const [fotoFallida, setFotoFallida] = useState(false);
  const clase = `avatar avatar--${tamano}`;

  if (fotoUrl && !fotoFallida) {
    return <img className={clase} src={fotoUrl} alt="" onError={() => setFotoFallida(true)} />;
  }
  return (
    <span className={clase} aria-hidden="true">
      {iniciales(nombre)}
    </span>
  );
}
