/** Íconos de línea usados en la interfaz; son decorativos (aria-hidden). */

interface Props {
  className?: string;
}

const base = {
  width: 20,
  height: 20,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 1.8,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
  'aria-hidden': true,
};

export function IconoReloj({ className }: Props) {
  return (
    <svg {...base} className={className} width={14} height={14}>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </svg>
  );
}

export function IconoCheck({ className }: Props) {
  return (
    <svg {...base} className={className} strokeWidth={2.4}>
      <path d="M5 12.5l4.5 4.5L19 7.5" />
    </svg>
  );
}

/** Tijeras abiertas: el símbolo de la barbería. */
export function IconoTijeras({ className }: Props) {
  return (
    <svg {...base} className={className}>
      <circle cx="6" cy="6.5" r="2.8" />
      <circle cx="6" cy="17.5" r="2.8" />
      <path d="M8.3 8.2L20 18M8.3 15.8L20 6" />
    </svg>
  );
}

export function IconoCalendario({ className }: Props) {
  return (
    <svg {...base} className={className}>
      <rect x="3.5" y="5" width="17" height="15" rx="2.5" />
      <path d="M3.5 10h17M8 3v4M16 3v4" />
    </svg>
  );
}

export function IconoMas({ className }: Props) {
  return (
    <svg {...base} className={className}>
      <path d="M12 5v14M5 12h14" />
    </svg>
  );
}

export function IconoMonedas({ className }: Props) {
  return (
    <svg {...base} className={className}>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M14.5 9.3c-.5-.8-1.4-1.3-2.5-1.3-1.5 0-2.6.8-2.6 2s1.1 1.7 2.6 2 2.6.8 2.6 2-1.1 2-2.6 2c-1.1 0-2-.5-2.5-1.3M12 6.5V8M12 16v1.5" />
    </svg>
  );
}

export function IconoGrafica({ className }: Props) {
  return (
    <svg {...base} className={className}>
      <path d="M4 20h16M7 16v-5M12 16V7M17 16v-8" />
    </svg>
  );
}

export function IconoRecibo({ className }: Props) {
  return (
    <svg {...base} className={className}>
      <path d="M6 3.5h12v17l-3-2-3 2-3-2-3 2z" />
      <path d="M9 8.5h6M9 12h6" />
    </svg>
  );
}

export function IconoSalir({ className }: Props) {
  return (
    <svg {...base} className={className}>
      <path d="M14 4h4.5A1.5 1.5 0 0120 5.5v13a1.5 1.5 0 01-1.5 1.5H14" />
      <path d="M10 8l-4 4 4 4M6 12h10" />
    </svg>
  );
}

export function IconoInicio({ className }: Props) {
  return (
    <svg {...base} className={className}>
      <path d="M4 10.5L12 4l8 6.5V19a1 1 0 01-1 1h-4.5v-5.5h-5V20H5a1 1 0 01-1-1z" />
    </svg>
  );
}
