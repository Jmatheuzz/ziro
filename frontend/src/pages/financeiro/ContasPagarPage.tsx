import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import { AppHeader } from "@/components/layout/AppHeader";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { BackLink } from "@/components/ui/BackLink";
import { StatusContaBadge } from "@/components/financeiro/StatusContaBadge";
import { contaPagarApi } from "@/api/financeiro";
import { extrairMensagemErro } from "@/api/errors";
import { formatarData, formatarMoeda } from "@/utils/formatters";
import type { ContaPagarResponse, StatusConta } from "@/types/financeiro";
import type { PaginaResponse } from "@/types/cliente";

const TAMANHO_PAGINA = 20;

const FILTROS: { valor: StatusConta | "TODAS"; rotulo: string }[] = [
  { valor: "ABERTA", rotulo: "Em aberto" },
  { valor: "PAGA", rotulo: "Pagas" },
  { valor: "CANCELADA", rotulo: "Canceladas" },
  { valor: "TODAS", rotulo: "Todas" },
];

export function ContasPagarPage() {
  const [filtro, setFiltro] = useState<StatusConta | "TODAS">("ABERTA");
  const [pagina, setPagina] = useState(0);
  const [dados, setDados] = useState<PaginaResponse<ContaPagarResponse> | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [moduloInativo, setModuloInativo] = useState(false);
  const [acaoEmAndamento, setAcaoEmAndamento] = useState<string | null>(null);
  const [confirmandoCancelamento, setConfirmandoCancelamento] = useState<string | null>(null);

  useEffect(() => {
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filtro, pagina]);

  async function carregar() {
    setCarregando(true);
    setErro(null);
    setModuloInativo(false);

    try {
      const resposta = await contaPagarApi.listar({
        status: filtro === "TODAS" ? undefined : filtro,
        page: pagina,
        size: TAMANHO_PAGINA,
      });
      setDados(resposta);
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 403) setModuloInativo(true);
      setErro(extrairMensagemErro(err, "Não deu pra carregar as contas a pagar"));
    } finally {
      setCarregando(false);
    }
  }

  async function handleMarcarPaga(id: string) {
    setAcaoEmAndamento(id);
    try {
      await contaPagarApi.marcarComoPaga(id);
      carregar();
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra marcar essa conta como paga"));
    } finally {
      setAcaoEmAndamento(null);
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
      await contaPagarApi.cancelar(id);
      carregar();
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra cancelar essa conta"));
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
        <BackLink to="/financeiro">Financeiro</BackLink>

        <div className="mt-3 flex flex-wrap items-center justify-between gap-4">
          <h1 className="font-display text-2xl font-semibold text-ink">Contas a pagar</h1>
          <Link to="/financeiro/contas-a-pagar/nova">
            <Button className="w-auto px-4">Nova conta</Button>
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
            <p className="text-sm text-ink-400">Nenhuma conta encontrada nesse filtro.</p>
          </div>
        ) : (
          <>
            <div className="mt-4 overflow-hidden rounded-xl border border-ink-100 bg-white shadow-card">
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-ink-100 text-ink-400">
                    <th className="px-4 py-3 font-medium">Descrição</th>
                    <th className="px-4 py-3 font-medium">Vencimento</th>
                    <th className="px-4 py-3 font-medium">Valor</th>
                    <th className="px-4 py-3 font-medium">Status</th>
                    <th className="px-4 py-3 font-medium"></th>
                  </tr>
                </thead>
                <tbody>
                  {dados?.content.map((conta) => (
                    <tr key={conta.id} className="border-b border-ink-100 last:border-0">
                      <td className="px-4 py-3 font-medium text-ink">
                        {conta.descricao}
                        {conta.fornecedor && <span className="block text-xs text-ink-400">{conta.fornecedor}</span>}
                      </td>
                      <td className={`px-4 py-3 ${conta.atrasada ? "font-medium text-rust" : "text-ink-400"}`}>
                        {formatarData(conta.dataVencimento)}
                      </td>
                      <td className="px-4 py-3 text-ink-400">{formatarMoeda(conta.valor)}</td>
                      <td className="px-4 py-3">
                        <StatusContaBadge status={conta.status} atrasada={conta.atrasada} />
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex justify-end gap-3">
                          {conta.status === "ABERTA" && (
                            <>
                              <Link to={`/financeiro/contas-a-pagar/${conta.id}/editar`} className="text-xs font-medium text-ink-400 hover:text-ink">
                                Editar
                              </Link>
                              <button
                                onClick={() => handleMarcarPaga(conta.id)}
                                disabled={acaoEmAndamento === conta.id}
                                className="text-xs font-medium text-sage hover:text-sage/80 disabled:opacity-50"
                              >
                                Marcar paga
                              </button>
                              <button
                                onClick={() => handleCancelar(conta.id)}
                                disabled={acaoEmAndamento === conta.id}
                                className={`text-xs font-medium disabled:opacity-50 ${
                                  confirmandoCancelamento === conta.id ? "text-rust" : "text-ink-400 hover:text-rust"
                                }`}
                              >
                                {confirmandoCancelamento === conta.id ? "Confirmar?" : "Cancelar"}
                              </button>
                            </>
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
