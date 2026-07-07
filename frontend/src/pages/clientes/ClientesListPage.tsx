import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import { AppHeader } from "@/components/layout/AppHeader";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { clienteApi } from "@/api/clientes";
import { extrairMensagemErro } from "@/api/errors";
import type { ClienteResponse, PaginaResponse } from "@/types/cliente";

const TAMANHO_PAGINA = 20;

export function ClientesListPage() {
  const [busca, setBusca] = useState("");
  const [buscaDebounced, setBuscaDebounced] = useState("");
  const [pagina, setPagina] = useState(0);

  const [dados, setDados] = useState<PaginaResponse<ClienteResponse> | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [moduloInativo, setModuloInativo] = useState(false);
  const [confirmandoExclusao, setConfirmandoExclusao] = useState<string | null>(null);

  // debounce simples da busca, pra nao disparar uma request a cada tecla
  useEffect(() => {
    const timeout = setTimeout(() => {
      setBuscaDebounced(busca);
      setPagina(0);
    }, 350);
    return () => clearTimeout(timeout);
  }, [busca]);

  useEffect(() => {
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [buscaDebounced, pagina]);

  async function carregar() {
    setCarregando(true);
    setErro(null);
    setModuloInativo(false);

    try {
      const resposta = await clienteApi.listar({ busca: buscaDebounced, page: pagina, size: TAMANHO_PAGINA });
      setDados(resposta);
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 403) {
        setModuloInativo(true);
        setErro(extrairMensagemErro(err));
      } else {
        setErro(extrairMensagemErro(err, "Não deu pra carregar os clientes"));
      }
    } finally {
      setCarregando(false);
    }
  }

  async function handleExcluir(id: string) {
    if (confirmandoExclusao !== id) {
      setConfirmandoExclusao(id);
      return;
    }

    setConfirmandoExclusao(null);
    try {
      await clienteApi.excluir(id);
      carregar();
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra excluir esse cliente"));
    }
  }

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
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="font-display text-2xl font-semibold text-ink">Clientes</h1>
            <p className="mt-1 text-sm text-ink-400">
              {dados ? `${dados.totalElements} cliente${dados.totalElements === 1 ? "" : "s"} cadastrado${dados.totalElements === 1 ? "" : "s"}` : ""}
            </p>
          </div>
          <Link to="/clientes/novo">
            <Button className="w-auto px-4">Novo cliente</Button>
          </Link>
        </div>

        <input
          type="text"
          placeholder="Buscar por nome..."
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
          className="mt-6 w-full max-w-sm rounded-lg border border-ink-100 bg-white px-3.5 py-2.5 text-sm
            placeholder:text-ink-400/60 focus:outline-none focus:ring-2 focus:ring-brass/40"
        />

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
            <p className="text-sm text-ink-400">
              {buscaDebounced ? "Nenhum cliente encontrado com esse nome." : "Nenhum cliente cadastrado ainda."}
            </p>
            {!buscaDebounced && (
              <Link to="/clientes/novo" className="mt-3 inline-block text-sm font-medium text-ink hover:text-brass-700">
                Cadastrar o primeiro cliente
              </Link>
            )}
          </div>
        ) : (
          <>
            <div className="mt-6 overflow-hidden rounded-xl border border-ink-100 bg-white shadow-card">
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-ink-100 text-ink-400">
                    <th className="px-4 py-3 font-medium">Nome</th>
                    <th className="px-4 py-3 font-medium">Telefone</th>
                    <th className="px-4 py-3 font-medium">Email</th>
                    <th className="px-4 py-3 font-medium"></th>
                  </tr>
                </thead>
                <tbody>
                  {dados?.content.map((cliente) => (
                    <tr key={cliente.id} className="border-b border-ink-100 last:border-0">
                      <td className="px-4 py-3 font-medium text-ink">{cliente.nome}</td>
                      <td className="px-4 py-3 text-ink-400">{cliente.telefone || "—"}</td>
                      <td className="px-4 py-3 text-ink-400">{cliente.email || "—"}</td>
                      <td className="px-4 py-3 text-right">
                        <div className="flex justify-end gap-3">
                          <Link
                            to={`/clientes/${cliente.id}/editar`}
                            className="text-xs font-medium text-ink-400 hover:text-ink"
                          >
                            Editar
                          </Link>
                          <button
                            onClick={() => handleExcluir(cliente.id)}
                            className={`text-xs font-medium ${
                              confirmandoExclusao === cliente.id ? "text-rust" : "text-ink-400 hover:text-rust"
                            }`}
                          >
                            {confirmandoExclusao === cliente.id ? "Confirmar?" : "Excluir"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {dados && dados.totalPages > 1 && (
              <div className="mt-4 flex items-center justify-between text-sm text-ink-400">
                <span>
                  Página {dados.number + 1} de {dados.totalPages}
                </span>
                <div className="flex gap-2">
                  <button
                    onClick={() => setPagina((p) => Math.max(0, p - 1))}
                    disabled={dados.first}
                    className="rounded-md border border-ink-100 px-3 py-1 disabled:opacity-40"
                  >
                    Anterior
                  </button>
                  <button
                    onClick={() => setPagina((p) => p + 1)}
                    disabled={dados.last}
                    className="rounded-md border border-ink-100 px-3 py-1 disabled:opacity-40"
                  >
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
