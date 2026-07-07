import { useEffect, useState, type FormEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { BackLink } from "@/components/ui/BackLink";
import { produtoApi, categoriaApi } from "@/api/estoque";
import { extrairMensagemErro } from "@/api/errors";
import type { CategoriaResponse } from "@/types/estoque";

export function ProdutoFormPage() {
  const { id } = useParams();
  const editando = !!id;
  const navigate = useNavigate();

  const [nome, setNome] = useState("");
  const [descricao, setDescricao] = useState("");
  const [precoVenda, setPrecoVenda] = useState("");
  const [precoCusto, setPrecoCusto] = useState("");
  const [quantidadeEstoque, setQuantidadeEstoque] = useState("0");
  const [estoqueMinimo, setEstoqueMinimo] = useState("");
  const [sku, setSku] = useState("");
  const [categoriaId, setCategoriaId] = useState("");

  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [novaCategoria, setNovaCategoria] = useState("");
  const [criandoCategoria, setCriandoCategoria] = useState(false);
  const [mostrarNovaCategoria, setMostrarNovaCategoria] = useState(false);

  const [carregandoDados, setCarregandoDados] = useState(editando);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    carregarCategorias();
  }, []);

  useEffect(() => {
    if (!id) return;
    produtoApi
      .buscar(id)
      .then((produto) => {
        setNome(produto.nome);
        setDescricao(produto.descricao ?? "");
        setPrecoVenda(String(produto.precoVenda));
        setPrecoCusto(produto.precoCusto != null ? String(produto.precoCusto) : "");
        setEstoqueMinimo(produto.estoqueMinimo != null ? String(produto.estoqueMinimo) : "");
        setSku(produto.sku ?? "");
        setCategoriaId(produto.categoriaId ?? "");
      })
      .catch((err) => setErro(extrairMensagemErro(err, "Não deu pra carregar esse produto")))
      .finally(() => setCarregandoDados(false));
  }, [id]);

  async function carregarCategorias() {
    try {
      const lista = await categoriaApi.listar();
      setCategorias(lista);
    } catch {
      setCategorias([]);
    }
  }

  async function handleCriarCategoria() {
    if (!novaCategoria.trim()) return;
    setCriandoCategoria(true);
    try {
      const categoria = await categoriaApi.criar({ nome: novaCategoria.trim() });
      setCategorias((atuais) => [...atuais, categoria]);
      setCategoriaId(categoria.id);
      setNovaCategoria("");
      setMostrarNovaCategoria(false);
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra criar essa categoria"));
    } finally {
      setCriandoCategoria(false);
    }
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);

    const precoVendaNumerico = Number(precoVenda.replace(",", "."));
    if (!precoVendaNumerico || precoVendaNumerico <= 0) {
      setErro("Informe um preço de venda válido, maior que zero");
      return;
    }

    setSalvando(true);
    try {
      if (editando && id) {
        await produtoApi.atualizar(id, {
          nome,
          descricao: descricao || undefined,
          precoVenda: precoVendaNumerico,
          precoCusto: precoCusto ? Number(precoCusto.replace(",", ".")) : undefined,
          estoqueMinimo: estoqueMinimo ? Number(estoqueMinimo) : undefined,
          sku: sku || undefined,
          categoriaId: categoriaId || undefined,
        });
      } else {
        await produtoApi.criar({
          nome,
          descricao: descricao || undefined,
          precoVenda: precoVendaNumerico,
          precoCusto: precoCusto ? Number(precoCusto.replace(",", ".")) : undefined,
          quantidadeEstoque: Number(quantidadeEstoque) || 0,
          estoqueMinimo: estoqueMinimo ? Number(estoqueMinimo) : undefined,
          sku: sku || undefined,
          categoriaId: categoriaId || undefined,
        });
      }
      navigate("/estoque/produtos", { replace: true });
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra salvar esse produto"));
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-lg px-6 py-12">
        <BackLink to="/estoque/produtos">Voltar pra produtos</BackLink>

        <h1 className="mt-3 font-display text-2xl font-semibold text-ink">
          {editando ? "Editar produto" : "Novo produto"}
        </h1>

        {carregandoDados ? (
          <div className="mt-10 flex justify-center">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-ink-100 border-t-ink" />
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-4">
            {erro && <Alert tipo="erro">{erro}</Alert>}

            <Input label="Nome" name="nome" value={nome} onChange={(e) => setNome(e.target.value)} required />

            <div className="flex flex-col gap-1.5">
              <label htmlFor="descricao" className="text-sm font-medium text-ink-600">
                Descrição
              </label>
              <textarea
                id="descricao"
                value={descricao}
                onChange={(e) => setDescricao(e.target.value)}
                rows={2}
                className="rounded-lg border border-ink-100 bg-white px-3.5 py-2.5 text-sm placeholder:text-ink-400/60
                  focus:outline-none focus:ring-2 focus:ring-brass/40"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Input label="Preço de venda (R$)" name="precoVenda" inputMode="decimal" value={precoVenda} onChange={(e) => setPrecoVenda(e.target.value)} placeholder="0,00" required />
              <Input label="Preço de custo (R$)" name="precoCusto" inputMode="decimal" value={precoCusto} onChange={(e) => setPrecoCusto(e.target.value)} placeholder="0,00" />
            </div>

            {!editando && (
              <Input
                label="Quantidade inicial em estoque"
                name="quantidadeEstoque"
                type="number"
                min={0}
                value={quantidadeEstoque}
                onChange={(e) => setQuantidadeEstoque(e.target.value)}
              />
            )}

            <Input
              label="Estoque mínimo (opcional)"
              name="estoqueMinimo"
              type="number"
              min={0}
              value={estoqueMinimo}
              onChange={(e) => setEstoqueMinimo(e.target.value)}
              placeholder="Usa o padrão da empresa se deixar em branco"
            />

            <Input label="SKU (opcional)" name="sku" value={sku} onChange={(e) => setSku(e.target.value)} />

            <div className="flex flex-col gap-1.5">
              <label htmlFor="categoria" className="text-sm font-medium text-ink-600">
                Categoria (opcional)
              </label>
              <select
                id="categoria"
                value={categoriaId}
                onChange={(e) => setCategoriaId(e.target.value)}
                className="rounded-lg border border-ink-100 bg-white px-3.5 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brass/40"
              >
                <option value="">Nenhuma</option>
                {categorias.map((categoria) => (
                  <option key={categoria.id} value={categoria.id}>
                    {categoria.nome}
                  </option>
                ))}
              </select>

              {mostrarNovaCategoria ? (
                <div className="mt-1 flex gap-2">
                  <input
                    type="text"
                    value={novaCategoria}
                    onChange={(e) => setNovaCategoria(e.target.value)}
                    placeholder="Nome da categoria"
                    className="flex-1 rounded-lg border border-ink-100 bg-white px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-brass/40"
                  />
                  <button
                    type="button"
                    onClick={handleCriarCategoria}
                    disabled={criandoCategoria}
                    className="rounded-lg bg-ink px-3 py-1.5 text-xs font-medium text-paper disabled:opacity-60"
                  >
                    Criar
                  </button>
                </div>
              ) : (
                <button
                  type="button"
                  onClick={() => setMostrarNovaCategoria(true)}
                  className="mt-1 self-start text-xs font-medium text-ink-400 hover:text-ink"
                >
                  + Nova categoria
                </button>
              )}
            </div>

            <Button type="submit" carregando={salvando} className="mt-2">
              {editando ? "Salvar alterações" : "Cadastrar produto"}
            </Button>
          </form>
        )}
      </main>
    </div>
  );
}
