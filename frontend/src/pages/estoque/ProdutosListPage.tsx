import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import { AppHeader } from "@/components/layout/AppHeader";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { BackLink } from "@/components/ui/BackLink";
import { produtoApi } from "@/api/estoque";
import { extrairMensagemErro } from "@/api/errors";
import { formatarMoeda } from "@/utils/formatters";
import type { ProdutoResponse } from "@/types/estoque";
import type { PaginaResponse } from "@/types/cliente";

const TAMANHO_PAGINA = 20;

export function ProdutosListPage() {
  const [busca, setBusca] = useState("");
  const [buscaDebounced, setBuscaDebounced] = useState("");
  const [pagina, setPagina] = useState(0);

  const [dados, setDados] = useState<PaginaResponse<ProdutoResponse> | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [moduloInativo, setModuloInativo] = useState(false);
  const [confirmandoExclusao, setConfirmandoExclusao] = useState<string | null>(null);

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
      const resposta = await produtoApi.listar({ busca: buscaDebounced, page: pagina, size: TAMANHO_PAGINA });
      setDados(resposta);
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 403) setModuloInativo(true);
      setErro(extrairMensagemErro(err, "Não deu pra carregar os produtos"));
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
      await produtoApi.excluir(id);
      carregar();
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra excluir esse produto"));
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
        <BackLink to="/estoque">Estoque</BackLink>

        <div className="mt-3 flex flex-wrap items-center justify-between gap-4">
          <h1 className="font-display text-2xl font-semibold text-ink">Produtos</h1>
          <Link to="/estoque/produtos/novo">
            <Button className="w-auto px-4">Novo produto</Button>
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
              {buscaDebounced ? "Nenhum produto encontrado com esse nome." : "Nenhum produto cadastrado ainda."}
            </p>
            {!buscaDebounced && (
              <Link to="/estoque/produtos/novo" className="mt-3 inline-block text-sm font-medium text-ink hover:text-brass-700">
                Cadastrar o primeiro produto
              </Link>
            )}
          </div>
        ) : (
          <>
            <div className="mt-6 overflow-hidden rounded-xl border border-ink-100 bg-white shadow-card">
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-ink-100 text-ink-400">
                    <th className="px-4 py-3 font-medium">Produto</th>
                    <th className="px-4 py-3 font-medium">Categoria</th>
                    <th className="px-4 py-3 font-medium">Preço</th>
                    <th className="px-4 py-3 font-medium">Estoque</th>
                    <th className="px-4 py-3 font-medium"></th>
                  </tr>
                </thead>
                <tbody>
                  {dados?.content.map((produto) => (
                    <tr key={produto.id} className="border-b border-ink-100 last:border-0">
                      <td className="px-4 py-3 font-medium text-ink">
                        {produto.nome}
                        {produto.sku && <span className="block text-xs text-ink-400">SKU: {produto.sku}</span>}
                      </td>
                      <td className="px-4 py-3 text-ink-400">{produto.categoriaNome || "—"}</td>
                      <td className="px-4 py-3 text-ink-400">{formatarMoeda(produto.precoVenda)}</td>
                      <td className="px-4 py-3">
                        <span className={`font-medium ${produto.estoqueBaixo ? "text-rust" : "text-ink"}`}>
                          {produto.quantidadeEstoque ?? "—"}
                        </span>
                        {produto.estoqueBaixo && (
                          <span className="ml-2 rounded-full bg-rust/10 px-2 py-0.5 text-xs font-medium text-rust">Baixo</span>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex justify-end gap-3">
                          <Link to={`/estoque/produtos/${produto.id}/ajustar`} className="text-xs font-medium text-ink-400 hover:text-ink">
                            Ajustar estoque
                          </Link>
                          <Link to={`/estoque/produtos/${produto.id}/editar`} className="text-xs font-medium text-ink-400 hover:text-ink">
                            Editar
                          </Link>
                          <button
                            onClick={() => handleExcluir(produto.id)}
                            className={`text-xs font-medium ${confirmandoExclusao === produto.id ? "text-rust" : "text-ink-400 hover:text-rust"}`}
                          >
                            {confirmandoExclusao === produto.id ? "Confirmar?" : "Excluir"}
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
