import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import { AppHeader } from "@/components/layout/AppHeader";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { vendaApi } from "@/api/vendas";
import { extrairMensagemErro } from "@/api/errors";
import { formatarMoeda } from "@/utils/formatters";
import type { VendasResumoResponse } from "@/types/venda";

export function VendasOverviewPage() {
  const [resumo, setResumo] = useState<VendasResumoResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [moduloInativo, setModuloInativo] = useState(false);

  useEffect(() => {
    vendaApi
      .resumo()
      .then(setResumo)
      .catch((err) => {
        if (isAxiosError(err) && err.response?.status === 403) setModuloInativo(true);
        setErro(extrairMensagemErro(err, "Não deu pra carregar o resumo de vendas"));
      })
      .finally(() => setCarregando(false));
  }, []);

  if (moduloInativo) {
    return (
      <div className="min-h-screen bg-paper">
        <AppHeader />
        <main className="mx-auto max-w-2xl px-6 py-16">
          <Alert tipo="erro">{erro}</Alert>
          <Link to="/configuracoes/personalizacao" className="mt-4 inline-block text-sm font-medium text-ink hover:text-brass-700">
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
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="font-display text-2xl font-semibold text-ink">Vendas</h1>
            <p className="mt-2 text-sm text-ink-400">O que entrou pelo balcão.</p>
          </div>
          <Link to="/vendas/nova">
            <Button className="w-auto px-4">Nova venda</Button>
          </Link>
        </div>

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
            <div className="mt-8 grid gap-4 sm:grid-cols-3">
              <div className="rounded-xl border border-ink-100 bg-white p-4 shadow-card">
                <p className="text-xs font-medium text-ink-400">Vendido no mês</p>
                <p className="mt-1 font-display text-xl font-semibold text-ink">{formatarMoeda(resumo.totalVendidoMesAtual)}</p>
              </div>
              <div className="rounded-xl border border-ink-100 bg-white p-4 shadow-card">
                <p className="text-xs font-medium text-ink-400">Vendas no mês</p>
                <p className="mt-1 font-display text-xl font-semibold text-ink">{resumo.quantidadeVendasMesAtual}</p>
              </div>
              <div className="rounded-xl border border-ink-100 bg-white p-4 shadow-card">
                <p className="text-xs font-medium text-ink-400">Ticket médio</p>
                <p className="mt-1 font-display text-xl font-semibold text-ink">{formatarMoeda(resumo.ticketMedioMesAtual)}</p>
              </div>
            </div>
          )
        )}

        <div className="mt-8">
          <Link
            to="/vendas/historico"
            className="inline-block rounded-xl border border-ink-100 bg-white p-5 shadow-card transition-standard hover:border-ink-400"
          >
            <h2 className="font-display text-base font-semibold text-ink">Ver histórico de vendas →</h2>
          </Link>
        </div>
      </main>
    </div>
  );
}
