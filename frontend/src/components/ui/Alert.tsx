import type { ReactNode } from "react";

interface AlertProps {
  tipo?: "erro" | "sucesso";
  children: ReactNode;
}

export function Alert({ tipo = "erro", children }: AlertProps) {
  const estilos = {
    erro: "bg-rust/10 text-rust border-rust/20",
    sucesso: "bg-sage/10 text-sage border-sage/20",
  };

  return (
    <div className={`rounded-lg border px-3.5 py-2.5 text-sm ${estilos[tipo]}`} role="alert">
      {children}
    </div>
  );
}
