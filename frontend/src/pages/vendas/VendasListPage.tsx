import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import { AppHeader } from "@/components/layout/AppHeader";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { BackLink } from "@/components/ui/BackLink";
import { vendaApi } from "@/api/vendas";
import { extrairMensagemErro } from "@/api/errors";
import { formatarData, formatarMoeda } from "@/utils/formatters";
import type { StatusVenda, VendaResponse } from "@/types/venda";
import type { PaginaResponse } from "@/types/cliente";

const TAMANHO_PAGINA = 20;

const FILTROS: { valor: StatusVenda | "TODAS"; rotulo: string }[] = [
  { valor: "ATIVA", rotulo: "Ativas" },
  { valor: "CANCELADA", rotulo: "Canceladas" },
  { valor: "TODAS", rotulo: "Todas" },
];

const ROTULOS_FORMA_PAGAMENTO: Record<string, string> = {
  DINHEIRO: "Dinheiro",
  CARTAO: "Cartão",
  PIX: "Pix",
  FIADO: "Fiado",
};

export function VendasListPage() {
  const [filtro, setFiltro] = useState<StatusVenda | "TODAS">("ATIVA");
  const [pagina, setPagina] = useState(0);
  const [dados, setDados] = useState<PaginaResponse<VendaResponse> | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [moduloInativo, setModuloInativo] = useState(false);
  const [confirmandoCancelamento, setConfirmandoCancelamento] = useState<string | null>(null);
  const [acaoEmAndamento, setAcaoEmAndamento] = useState<string | null>(null);

  useEffect(() => {
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filtro, pagina]);

  async function carregar() {
    setCarregando(true);
    setErro(null);
    setModuloInativo(false);
    try {
      const resposta = await vendaApi.listar({
        status: filtro === "TODAS" ? undefined : filtro,
        page: pagina,
        size: TAMANHO_PAGINA,
      });
      setDados(resposta);
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 403) setModuloInativo(true);
      setErro(extrairMensagemErro(err, "Não deu pra carregar as vendas"));
    } finally {
      setCarregando(false);
    }
  }

  async function handleCancelar(id: string) {
    if (confirmandoCancelamento !== id) {
      setConfirmandoCancelamento(id);
      return;
    }
    setConfirmandoCancelamento(null);
    setAcaoEmAndamento(id);
    try {
      await vendaApi.cancelar(id);
      carregar();
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra cancelar essa venda"));
    } finally {
      setAcaoEmAndamento(null);
    }
  }

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
        <BackLink to="/vendas">Vendas</BackLink>

        <div className="mt-3 flex flex-wrap items-center justify-between gap-4">
          <h1 className="font-display text-2xl font-semibold text-ink">Histórico de vendas</h1>
          <Link to="/vendas/nova">
            <Button className="w-auto px-4">Nova venda</Button>
          </Link>
        </div>

        <div className="mt-6 flex gap-1">
          {FILTROS.map((f) => (
            <button
              key={f.valor}
              onClick={() => {
                setFiltro(f.valor);
                setPagina(0);
              }}
              className={`rounded-md px-3 py-1.5 text-sm font-medium transition-standard ${
                filtro === f.valor ? "bg-ink-50 text-ink" : "text-ink-400 hover:text-ink"
              }`}
            >
              {f.rotulo}
            </button>
          ))}
        </div>

        {erro && !moduloInativo && (
          <div className="mt-4">
            <Alert tipo="erro">{erro}</Alert>
          </div>
        )}

        {carregando ? (
          <div className="mt-10 flex justify-center">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-ink-100 border-t-ink" />
          </div>
        ) : dados && dados.content.length === 0 ? (
          <div className="mt-16 text-center">
            <p className="text-sm text-ink-400">Nenhuma venda encontrada nesse filtro.</p>
          </div>
        ) : (
          <>
            <div className="mt-4 overflow-hidden rounded-xl border border-ink-100 bg-white shadow-card">
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-ink-100 text-ink-400">
                    <th className="px-4 py-3 font-medium">Data</th>
                    <th className="px-4 py-3 font-medium">Cliente</th>
                    <th className="px-4 py-3 font-medium">Pagamento</th>
                    <th className="px-4 py-3 font-medium">Valor</th>
                    <th className="px-4 py-3 font-medium">Status</th>
                    <th className="px-4 py-3 font-medium"></th>
                  </tr>
                </thead>
                <tbody>
                  {dados?.content.map((venda) => (
                    <tr key={venda.id} className="border-b border-ink-100 last:border-0">
                      <td className="px-4 py-3 text-ink-400">{formatarData(venda.dataVenda)}</td>
                      <td className="px-4 py-3 text-ink">{venda.clienteNome || "Consumidor final"}</td>
                      <td className="px-4 py-3 text-ink-400">{ROTULOS_FORMA_PAGAMENTO[venda.formaPagamento]}</td>
                      <td className="px-4 py-3 font-medium text-ink">{formatarMoeda(venda.valorTotal)}</td>
                      <td className="px-4 py-3">
                        <span
                          className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                            venda.status === "CANCELADA" ? "bg-ink-50 text-ink-400" : "bg-sage/10 text-sage"
                          }`}
                        >
                          {venda.status === "CANCELADA" ? "Cancelada" : "Ativa"}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex justify-end gap-3">
                          <Link to={`/vendas/${venda.id}`} className="text-xs font-medium text-ink-400 hover:text-ink">
                            Ver detalhes
                          </Link>
                          {venda.status === "ATIVA" && (
                            <button
                              onClick={() => handleCancelar(venda.id)}
                              disabled={acaoEmAndamento === venda.id}
                              className={`text-xs font-medium disabled:opacity-50 ${
                                confirmandoCancelamento === venda.id ? "text-rust" : "text-ink-400 hover:text-rust"
                              }`}
                            >
                              {confirmandoCancelamento === venda.id ? "Confirmar?" : "Cancelar"}
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {dados && dados.totalPages > 1 && (
              <div className="mt-4 flex items-center justify-between text-sm text-ink-400">
                <span>Página {dados.number + 1} de {dados.totalPages}</span>
                <div className="flex gap-2">
                  <button onClick={() => setPagina((p) => Math.max(0, p - 1))} disabled={dados.first} className="rounded-md border border-ink-100 px-3 py-1 disabled:opacity-40">
                    Anterior
                  </button>
                  <button onClick={() => setPagina((p) => p + 1)} disabled={dados.last} className="rounded-md border border-ink-100 px-3 py-1 disabled:opacity-40">
                    Próxima
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
