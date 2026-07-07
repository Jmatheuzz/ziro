import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import { AppHeader } from "@/components/layout/AppHeader";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { estoqueResumoApi } from "@/api/estoque";
import { extrairMensagemErro } from "@/api/errors";
import { formatarMoeda } from "@/utils/formatters";
import type { EstoqueResumoResponse } from "@/types/estoque";

export function EstoqueOverviewPage() {
  const [resumo, setResumo] = useState<EstoqueResumoResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [moduloInativo, setModuloInativo] = useState(false);

  useEffect(() => {
    estoqueResumoApi
      .buscar()
      .then(setResumo)
      .catch((err) => {
        if (isAxiosError(err) && err.response?.status === 403) setModuloInativo(true);
        setErro(extrairMensagemErro(err, "Não deu pra carregar o resumo do estoque"));
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
            <h1 className="font-display text-2xl font-semibold text-ink">Estoque</h1>
            <p className="mt-2 text-sm text-ink-400">Seus produtos e o que está acabando.</p>
          </div>
          <Link to="/estoque/produtos/novo">
            <Button className="w-auto px-4">Novo produto</Button>
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
                <p className="text-xs font-medium text-ink-400">Produtos ativos</p>
                <p className="mt-1 font-display text-xl font-semibold text-ink">{resumo.totalProdutos}</p>
              </div>
              <div className="rounded-xl border border-ink-100 bg-white p-4 shadow-card">
                <p className="text-xs font-medium text-ink-400">Estoque baixo</p>
                <p className={`mt-1 font-display text-xl font-semibold ${resumo.produtosComEstoqueBaixo > 0 ? "text-rust" : "text-ink"}`}>
                  {resumo.produtosComEstoqueBaixo}
                </p>
              </div>
              <div className="rounded-xl border border-ink-100 bg-white p-4 shadow-card">
                <p className="text-xs font-medium text-ink-400">Valor em estoque</p>
                <p className="mt-1 font-display text-xl font-semibold text-ink">{formatarMoeda(resumo.valorTotalEstoque)}</p>
              </div>
            </div>
          )
        )}

        <div className="mt-8">
          <Link
            to="/estoque/produtos"
            className="inline-block rounded-xl border border-ink-100 bg-white p-5 shadow-card transition-standard hover:border-ink-400"
          >
            <h2 className="font-display text-base font-semibold text-ink">Ver todos os produtos →</h2>
          </Link>
        </div>
      </main>
    </div>
  );
}
