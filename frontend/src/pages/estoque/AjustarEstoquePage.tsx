import { useEffect, useState, type FormEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { BackLink } from "@/components/ui/BackLink";
import { produtoApi } from "@/api/estoque";
import { extrairMensagemErro } from "@/api/errors";
import type { ProdutoResponse, TipoMovimentacaoEstoque } from "@/types/estoque";

export function AjustarEstoquePage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [produto, setProduto] = useState<ProdutoResponse | null>(null);
  const [tipo, setTipo] = useState<TipoMovimentacaoEstoque>("ENTRADA");
  const [quantidade, setQuantidade] = useState("");
  const [motivo, setMotivo] = useState("");

  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    produtoApi
      .buscar(id)
      .then(setProduto)
      .catch((err) => setErro(extrairMensagemErro(err, "Não deu pra carregar esse produto")))
      .finally(() => setCarregando(false));
  }, [id]);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);

    const quantidadeNumerica = Number(quantidade);
    if (!quantidadeNumerica || quantidadeNumerica <= 0) {
      setErro("Informe uma quantidade válida, maior que zero");
      return;
    }
    if (!id) return;

    setSalvando(true);
    try {
      await produtoApi.ajustarEstoque(id, { tipo, quantidade: quantidadeNumerica, motivo: motivo || undefined });
      navigate("/estoque/produtos", { replace: true });
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra ajustar o estoque"));
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-md px-6 py-12">
        <BackLink to="/estoque/produtos">Voltar pra produtos</BackLink>

        <h1 className="mt-3 font-display text-2xl font-semibold text-ink">Ajustar estoque</h1>

        {carregando ? (
          <div className="mt-10 flex justify-center">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-ink-100 border-t-ink" />
          </div>
        ) : (
          produto && (
            <>
              <p className="mt-2 text-sm text-ink-400">
                <strong className="text-ink">{produto.nome}</strong> — estoque atual: {produto.quantidadeEstoque ?? 0}
              </p>

              <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-4">
                {erro && <Alert tipo="erro">{erro}</Alert>}

                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => setTipo("ENTRADA")}
                    className={`flex-1 rounded-lg border px-4 py-2 text-sm font-medium transition-standard ${
                      tipo === "ENTRADA" ? "border-sage bg-sage/10 text-sage" : "border-ink-100 text-ink-400"
                    }`}
                  >
                    Entrada
                  </button>
                  <button
                    type="button"
                    onClick={() => setTipo("SAIDA")}
                    className={`flex-1 rounded-lg border px-4 py-2 text-sm font-medium transition-standard ${
                      tipo === "SAIDA" ? "border-rust bg-rust/10 text-rust" : "border-ink-100 text-ink-400"
                    }`}
                  >
                    Saída
                  </button>
                </div>

                <Input
                  label="Quantidade"
                  name="quantidade"
                  type="number"
                  min={1}
                  value={quantidade}
                  onChange={(e) => setQuantidade(e.target.value)}
                  required
                />

                <Input
                  label="Motivo (opcional)"
                  name="motivo"
                  value={motivo}
                  onChange={(e) => setMotivo(e.target.value)}
                  placeholder="Ex: compra de mercadoria, perda, ajuste de inventário"
                />

                <Button type="submit" carregando={salvando}>
                  Confirmar ajuste
                </Button>
              </form>
            </>
          )
        )}
      </main>
    </div>
  );
}
