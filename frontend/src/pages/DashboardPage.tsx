import { Link } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { useModulos } from "@/context/ModulosContext";
import { AppHeader } from "@/components/layout/AppHeader";
import type { TipoModulo } from "@/types/empresa";

interface CardModulo {
  modulo: TipoModulo;
  titulo: string;
  descricao: string;
  rota: string;
}

const CARDS: CardModulo[] = [
  { modulo: "CLIENTES", titulo: "Clientes", descricao: "Cadastre e organize seus clientes", rota: "/clientes" },
  { modulo: "VENDAS", titulo: "Vendas", descricao: "Registre vendas do balcão", rota: "/vendas" },
  { modulo: "FINANCEIRO", titulo: "Financeiro", descricao: "Contas a pagar, a receber e fluxo de caixa", rota: "/financeiro" },
  { modulo: "ESTOQUE", titulo: "Estoque", descricao: "Produtos, quantidades e alertas", rota: "/estoque" },
];

export function DashboardPage() {
  const { usuario } = useAuth();
  const { moduloAtivo, carregando } = useModulos();

  const cardsAtivos = CARDS.filter((card) => moduloAtivo(card.modulo));

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-3xl px-6 py-16">
        <h1 className="font-display text-2xl font-semibold text-ink">
          Bem-vindo, {usuario?.nome.split(" ")[0]}
        </h1>
        <p className="mt-3 text-sm text-ink-400">Escolhe por onde começar.</p>

        {!carregando && cardsAtivos.length === 0 && (
          <p className="mt-8 text-sm text-ink-400">
            Nenhum módulo ativo ainda.{" "}
            <Link to="/configuracoes/personalizacao" className="font-medium text-ink hover:text-brass-700">
              Ative algum em Personalização
            </Link>{" "}
            pra começar a usar o Ziro.
          </p>
        )}

        <div className="mt-8 grid gap-4 sm:grid-cols-2">
          {cardsAtivos.map((card) => (
            <Link
              key={card.modulo}
              to={card.rota}
              className="rounded-xl border border-ink-100 bg-white p-5 shadow-card transition-standard hover:border-ink-400"
            >
              <h2 className="font-display text-base font-semibold text-ink">{card.titulo}</h2>
              <p className="mt-1 text-sm text-ink-400">{card.descricao}</p>
            </Link>
          ))}
        </div>
      </main>
    </div>
  );
}
