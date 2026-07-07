interface ZiroMarkProps {
  size?: number;
  className?: string;
}

/**
 * O "0" de Ziro se desenhando sozinho - a metafora visual de
 * "comeca do zero, sem complicacao". Usado com moderacao (um so lugar por tela).
 */
export function ZiroMark({ size = 96, className = "" }: ZiroMarkProps) {
  // perimetro aproximado da elipse (formula de Ramanujan), usado no stroke-dasharray
  const raioX = 34;
  const raioY = 44;
  const h = Math.pow(raioX - raioY, 2) / Math.pow(raioX + raioY, 2);
  const comprimento = Math.PI * (raioX + raioY) * (1 + (3 * h) / (10 + Math.sqrt(4 - 3 * h)));

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 100 100"
      fill="none"
      className={className}
      role="img"
      aria-label="Marca Ziro"
    >
      <ellipse
        cx="50"
        cy="50"
        rx={raioX}
        ry={raioY}
        stroke="currentColor"
        strokeWidth="6"
        strokeLinecap="round"
        className="ziro-mark-traco"
        style={{ ["--comprimento-traco" as string]: comprimento }}
      />
    </svg>
  );
}
