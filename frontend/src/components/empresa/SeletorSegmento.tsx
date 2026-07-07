import type { SegmentoNegocio } from "@/types/empresa";

interface OpcaoSegmento {
  valor: SegmentoNegocio;
  rotulo: string;
  descricao: string;
}

const OPCOES: OpcaoSegmento[] = [
  { valor: "COMERCIO", rotulo: "Comércio", descricao: "Vende produtos físicos" },
  { valor: "SERVICOS", rotulo: "Serviços", descricao: "Presta serviços, sem estoque" },
  { valor: "ALIMENTACAO", rotulo: "Alimentação", descricao: "Restaurante, lanchonete, delivery" },
  { valor: "OUTRO", rotulo: "Outro", descricao: "Nenhuma das opções acima" },
];

interface SeletorSegmentoProps {
  valor: SegmentoNegocio | null;
  onSelecionar: (segmento: SegmentoNegocio) => void;
}

export function SeletorSegmento({ valor, onSelecionar }: SeletorSegmentoProps) {
  return (
    <div className="grid grid-cols-2 gap-3">
      {OPCOES.map((opcao) => {
        const selecionado = valor === opcao.valor;
        return (
          <button
            key={opcao.valor}
            type="button"
            onClick={() => onSelecionar(opcao.valor)}
            className={`rounded-lg border px-4 py-3 text-left transition-standard
              ${selecionado ? "border-ink bg-ink-50" : "border-ink-100 bg-white hover:border-ink-400"}`}
          >
            <span className="block text-sm font-medium text-ink">{opcao.rotulo}</span>
            <span className="mt-0.5 block text-xs text-ink-400">{opcao.descricao}</span>
          </button>
        );
      })}
    </div>
  );
}
