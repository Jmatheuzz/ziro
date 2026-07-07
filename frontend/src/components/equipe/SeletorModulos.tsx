import type { TipoModulo } from "@/types/empresa";

const ROTULOS: Record<TipoModulo, string> = {
  CLIENTES: "Clientes",
  ESTOQUE: "Estoque",
  FINANCEIRO: "Financeiro",
  VENDAS: "Vendas",
};

interface SeletorModulosProps {
  modulosDisponiveis: TipoModulo[];
  selecionados: TipoModulo[];
  onChange: (modulos: TipoModulo[]) => void;
}

export function SeletorModulos({ modulosDisponiveis, selecionados, onChange }: SeletorModulosProps) {
  function alternar(modulo: TipoModulo) {
    if (selecionados.includes(modulo)) {
      onChange(selecionados.filter((m) => m !== modulo));
    } else {
      onChange([...selecionados, modulo]);
    }
  }

  if (modulosDisponiveis.length === 0) {
    return <p className="text-sm text-ink-400">Nenhum módulo ativo na empresa ainda.</p>;
  }

  return (
    <div className="flex flex-wrap gap-2">
      {modulosDisponiveis.map((modulo) => {
        const ativo = selecionados.includes(modulo);
        return (
          <button
            key={modulo}
            type="button"
            onClick={() => alternar(modulo)}
            className={`rounded-lg border px-3 py-1.5 text-sm font-medium transition-standard ${
              ativo ? "border-ink bg-ink-50 text-ink" : "border-ink-100 text-ink-400"
            }`}
          >
            {ROTULOS[modulo]}
          </button>
        );
      })}
    </div>
  );
}
