import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import { AppHeader } from "@/components/layout/AppHeader";
import { Alert } from "@/components/ui/Alert";
import { resumoFinanceiroApi } from "@/api/financeiro";
import { extrairMensagemErro } from "@/api/errors";
import { formatarMoeda } from "@/utils/formatters";
import type { ResumoFinanceiroResponse } from "@/types/financeiro";

export function FinanceiroOverviewPage() {
  const [resumo, setResumo] = useState<ResumoFinanceiroResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [moduloInativo, setModuloInativo] = useState(false);

  useEffect(() => {
    resumoFinanceiroApi
      .buscar()
      .then(setResumo)
      .catch((err) => {
        if (isAxiosError(err) && err.response?.status === 403) {
          setModuloInativo(true);
        }
        setErro(extrairMensagemErro(err, "Não deu pra carregar o resumo financeiro"));
      })
      .finally(() => setCarregando(false));
  }, []);

  if (moduloInativo) {
    return (
      <div className="min-h-screen bg-paper">
        <AppHeader />
        <main className="mx-auto max-w-2xl px-6 py-16">
          <Alert tipo="erro">{erro}</Alert>
          <Link
            to="/configuracoes/personalizacao"
            className="mt-4 inline-block text-sm font-medium text-ink hover:text-brass-700"
          >
            Ir pra Personalização
          </Link>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-4xl px-6 py-12">
        <h1 className="font-display text-2xl font-semibold text-ink">Financeiro</h1>
        <p className="mt-2 text-sm text-ink-400">Uma visão rápida de como está o caixa do seu negócio.</p>

        {erro && !moduloInativo && (
          <div className="mt-6">
            <Alert tipo="erro">{erro}</Alert>
          </div>
        )}

        {carregando ? (
          <div className="mt-10 flex justify-center">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-ink-100 border-t-ink" />
          </div>
        ) : (
          resumo && (
            <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <CartaoResumo
                titulo="Saldo do mês"
                valor={formatarMoeda(resumo.saldoCaixaMesAtual)}
                destaque={resumo.saldoCaixaMesAtual < 0 ? "negativo" : "positivo"}
              />
              <CartaoResumo titulo="A receber em aberto" valor={formatarMoeda(resumo.totalAReceberEmAberto)} />
              <CartaoResumo titulo="A pagar em aberto" valor={formatarMoeda(resumo.totalAPagarEmAberto)} />
              <CartaoResumo
                titulo="Contas vencidas"
                valor={`${resumo.contasPagarVencidas + resumo.contasReceberVencidas}`}
                destaque={resumo.contasPagarVencidas + resumo.contasReceberVencidas > 0 ? "negativo" : undefined}
              />
            </div>
          )
        )}

        <div className="mt-8 grid gap-4 sm:grid-cols-3">
          <Link
            to="/financeiro/contas-a-pagar"
            className="rounded-xl border border-ink-100 bg-white p-5 shadow-card transition-standard hover:border-ink-400"
          >
            <h2 className="font-display text-base font-semibold text-ink">Contas a pagar</h2>
            <p className="mt-1 text-sm text-ink-400">Fornecedores e despesas</p>
          </Link>

          <Link
            to="/financeiro/contas-a-receber"
            className="rounded-xl border border-ink-100 bg-white p-5 shadow-card transition-standard hover:border-ink-400"
          >
            <h2 className="font-display text-base font-semibold text-ink">Contas a receber</h2>
            <p className="mt-1 text-sm text-ink-400">Cobranças e recebimentos</p>
          </Link>

          <Link
            to="/financeiro/fluxo-caixa"
            className="rounded-xl border border-ink-100 bg-white p-5 shadow-card transition-standard hover:border-ink-400"
          >
            <h2 className="font-display text-base font-semibold text-ink">Fluxo de caixa</h2>
            <p className="mt-1 text-sm text-ink-400">Entradas e saídas do período</p>
          </Link>
        </div>
      </main>
    </div>
  );
}

function CartaoResumo({
  titulo,
  valor,
  destaque,
}: {
  titulo: string;
  valor: string;
  destaque?: "positivo" | "negativo";
}) {
  const corValor = destaque === "negativo" ? "text-rust" : destaque === "positivo" ? "text-sage" : "text-ink";

  return (
    <div className="rounded-xl border border-ink-100 bg-white p-4 shadow-card">
      <p className="text-xs font-medium text-ink-400">{titulo}</p>
      <p className={`mt-1 font-display text-xl font-semibold ${corValor}`}>{valor}</p>
    </div>
  );
}
