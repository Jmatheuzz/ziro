import { useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { BackLink } from "@/components/ui/BackLink";
import { vendaApi } from "@/api/vendas";
import { produtoApi } from "@/api/estoque";
import { clienteApi } from "@/api/clientes";
import { extrairMensagemErro } from "@/api/errors";
import { formatarMoeda, hojeISO } from "@/utils/formatters";
import type { FormaPagamento, ItemVendaPayload } from "@/types/venda";
import type { ProdutoResponse } from "@/types/estoque";
import type { ClienteResponse } from "@/types/cliente";

interface ItemCarrinho extends ItemVendaPayload {
  nomeProduto: string;
  precoUnitario: number;
}

const FORMAS: { valor: FormaPagamento; rotulo: string }[] = [
  { valor: "DINHEIRO", rotulo: "Dinheiro" },
  { valor: "CARTAO", rotulo: "Cartão" },
  { valor: "PIX", rotulo: "Pix" },
  { valor: "FIADO", rotulo: "Fiado" },
];

export function VendaFormPage() {
  const navigate = useNavigate();

  const [produtos, setProdutos] = useState<ProdutoResponse[]>([]);
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);

  const [produtoSelecionado, setProdutoSelecionado] = useState("");
  const [quantidadeSelecionada, setQuantidadeSelecionada] = useState("1");
  const [carrinho, setCarrinho] = useState<ItemCarrinho[]>([]);

  const [clienteId, setClienteId] = useState("");
  const [formaPagamento, setFormaPagamento] = useState<FormaPagamento>("DINHEIRO");
  const [dataVenda, setDataVenda] = useState(hojeISO());
  const [desconto, setDesconto] = useState("");
  const [observacoes, setObservacoes] = useState("");

  const [erro, setErro] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    produtoApi.listar({ size: 200 }).then((p) => setProdutos(p.content.filter((prod) => prod.ativo)));
    clienteApi.listar({ size: 200 }).then((p) => setClientes(p.content));
  }, []);

  function handleAdicionarItem() {
    setErro(null);
    const produto = produtos.find((p) => p.id === produtoSelecionado);
    const quantidade = Number(quantidadeSelecionada);

    if (!produto) {
      setErro("Escolhe um produto pra adicionar");
      return;
    }
    if (!quantidade || quantidade <= 0) {
      setErro("Quantidade inválida");
      return;
    }
    if (produto.quantidadeEstoque != null && produto.quantidadeEstoque < quantidade) {
      setErro(`Estoque insuficiente pra ${produto.nome}. Disponível: ${produto.quantidadeEstoque}`);
      return;
    }

    setCarrinho((atual) => {
      const existente = atual.find((item) => item.produtoId === produto.id);
      if (existente) {
        return atual.map((item) =>
          item.produtoId === produto.id ? { ...item, quantidade: item.quantidade + quantidade } : item
        );
      }
      return [...atual, { produtoId: produto.id, quantidade, nomeProduto: produto.nome, precoUnitario: produto.precoVenda }];
    });

    setProdutoSelecionado("");
    setQuantidadeSelecionada("1");
  }

  function handleRemoverItem(produtoId: string) {
    setCarrinho((atual) => atual.filter((item) => item.produtoId !== produtoId));
  }

  const totalItens = carrinho.reduce((soma, item) => soma + item.precoUnitario * item.quantidade, 0);
  const descontoNumerico = Number(desconto.replace(",", ".")) || 0;
  const totalEstimado = Math.max(0, totalItens - descontoNumerico);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);

    if (carrinho.length === 0) {
      setErro("Adiciona pelo menos um produto na venda");
      return;
    }
    if (formaPagamento === "FIADO" && !clienteId) {
      setErro("Venda fiado precisa de um cliente vinculado");
      return;
    }

    setSalvando(true);
    try {
      await vendaApi.criar({
        clienteId: clienteId || undefined,
        dataVenda,
        formaPagamento,
        desconto: descontoNumerico || undefined,
        observacoes: observacoes || undefined,
        itens: carrinho.map(({ produtoId, quantidade }) => ({ produtoId, quantidade })),
      });
      navigate("/vendas/historico", { replace: true });
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra registrar essa venda"));
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-2xl px-6 py-12">
        <BackLink to="/vendas">Voltar pra vendas</BackLink>

        <h1 className="mt-3 font-display text-2xl font-semibold text-ink">Nova venda</h1>

        {erro && (
          <div className="mt-4">
            <Alert tipo="erro">{erro}</Alert>
          </div>
        )}

        {/* montar carrinho */}
        <section className="mt-6 rounded-xl border border-ink-100 bg-white p-5 shadow-card">
          <h2 className="font-display text-base font-semibold text-ink">Itens</h2>

          <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-end">
            <div className="flex-1">
              <label htmlFor="produto" className="mb-1.5 block text-sm font-medium text-ink-600">
                Produto
              </label>
              <select
                id="produto"
                value={produtoSelecionado}
                onChange={(e) => setProdutoSelecionado(e.target.value)}
                className="w-full rounded-lg border border-ink-100 bg-white px-3.5 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brass/40"
              >
                <option value="">Selecione...</option>
                {produtos.map((produto) => (
                  <option key={produto.id} value={produto.id}>
                    {produto.nome} — {formatarMoeda(produto.precoVenda)}
                    {produto.quantidadeEstoque != null ? ` (estoque: ${produto.quantidadeEstoque})` : ""}
                  </option>
                ))}
              </select>
            </div>

            <div className="w-24">
              <label htmlFor="quantidade" className="mb-1.5 block text-sm font-medium text-ink-600">
                Qtd.
              </label>
              <input
                id="quantidade"
                type="number"
                min={1}
                value={quantidadeSelecionada}
                onChange={(e) => setQuantidadeSelecionada(e.target.value)}
                className="w-full rounded-lg border border-ink-100 bg-white px-3.5 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brass/40"
              />
            </div>

            <Button type="button" variant="ghost" className="w-auto border border-ink-100 px-4" onClick={handleAdicionarItem}>
              Adicionar
            </Button>
          </div>

          {carrinho.length > 0 && (
            <div className="mt-4 flex flex-col gap-2">
              {carrinho.map((item) => (
                <div key={item.produtoId} className="flex items-center justify-between rounded-lg bg-paper px-3 py-2 text-sm">
                  <span className="text-ink">
                    {item.quantidade}x {item.nomeProduto}
                  </span>
                  <div className="flex items-center gap-3">
                    <span className="text-ink-400">{formatarMoeda(item.precoUnitario * item.quantidade)}</span>
                    <button
                      type="button"
                      onClick={() => handleRemoverItem(item.produtoId)}
                      className="text-xs font-medium text-ink-400 hover:text-rust"
                    >
                      Remover
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-4">
          <div>
            <span className="mb-1.5 block text-sm font-medium text-ink-600">Forma de pagamento</span>
            <div className="grid grid-cols-4 gap-2">
              {FORMAS.map((forma) => (
                <button
                  key={forma.valor}
                  type="button"
                  onClick={() => setFormaPagamento(forma.valor)}
                  className={`rounded-lg border px-2 py-2 text-sm font-medium transition-standard ${
                    formaPagamento === forma.valor ? "border-ink bg-ink-50 text-ink" : "border-ink-100 text-ink-400"
                  }`}
                >
                  {forma.rotulo}
                </button>
              ))}
            </div>
          </div>

          {formaPagamento === "FIADO" && (
            <div className="flex flex-col gap-1.5">
              <label htmlFor="cliente" className="text-sm font-medium text-ink-600">
                Cliente (obrigatório pra fiado)
              </label>
              <select
                id="cliente"
                value={clienteId}
                onChange={(e) => setClienteId(e.target.value)}
                className="rounded-lg border border-ink-100 bg-white px-3.5 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brass/40"
              >
                <option value="">Selecione...</option>
                {clientes.map((cliente) => (
                  <option key={cliente.id} value={cliente.id}>
                    {cliente.nome}
                  </option>
                ))}
              </select>
            </div>
          )}

          {formaPagamento !== "FIADO" && clientes.length > 0 && (
            <div className="flex flex-col gap-1.5">
              <label htmlFor="clienteOpcional" className="text-sm font-medium text-ink-600">
                Cliente (opcional)
              </label>
              <select
                id="clienteOpcional"
                value={clienteId}
                onChange={(e) => setClienteId(e.target.value)}
                className="rounded-lg border border-ink-100 bg-white px-3.5 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brass/40"
              >
                <option value="">Consumidor final</option>
                {clientes.map((cliente) => (
                  <option key={cliente.id} value={cliente.id}>
                    {cliente.nome}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <Input label="Data da venda" type="date" name="dataVenda" value={dataVenda} onChange={(e) => setDataVenda(e.target.value)} required />
            <Input label="Desconto (R$)" name="desconto" inputMode="decimal" value={desconto} onChange={(e) => setDesconto(e.target.value)} placeholder="0,00" />
          </div>

          <div className="flex flex-col gap-1.5">
            <label htmlFor="observacoes" className="text-sm font-medium text-ink-600">
              Observações
            </label>
            <textarea
              id="observacoes"
              value={observacoes}
              onChange={(e) => setObservacoes(e.target.value)}
              rows={2}
              className="rounded-lg border border-ink-100 bg-white px-3.5 py-2.5 text-sm placeholder:text-ink-400/60
                focus:outline-none focus:ring-2 focus:ring-brass/40"
            />
          </div>

          <div className="flex items-center justify-between rounded-lg bg-ink-50 px-4 py-3">
            <span className="text-sm font-medium text-ink-600">Total estimado</span>
            <span className="font-display text-lg font-semibold text-ink">{formatarMoeda(totalEstimado)}</span>
          </div>

          <Button type="submit" carregando={salvando}>
            Registrar venda
          </Button>
        </form>
      </main>
    </div>
  );
}
