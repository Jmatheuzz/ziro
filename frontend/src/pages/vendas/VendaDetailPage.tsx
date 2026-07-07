import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AppHeader } from "@/components/layout/AppHeader";
import { Alert } from "@/components/ui/Alert";
import { BackLink } from "@/components/ui/BackLink";
import { vendaApi } from "@/api/vendas";
import { extrairMensagemErro } from "@/api/errors";
import { formatarData, formatarMoeda } from "@/utils/formatters";
import type { VendaResponse } from "@/types/venda";

const ROTULOS_FORMA_PAGAMENTO: Record<string, string> = {
  DINHEIRO: "Dinheiro",
  CARTAO: "Cartão",
  PIX: "Pix",
  FIADO: "Fiado",
};

export function VendaDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [venda, setVenda] = useState<VendaResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [confirmandoCancelamento, setConfirmandoCancelamento] = useState(false);
  const [cancelando, setCancelando] = useState(false);

  useEffect(() => {
    if (!id) return;
    vendaApi
      .buscar(id)
      .then(setVenda)
      .catch((err) => setErro(extrairMensagemErro(err, "Não deu pra carregar essa venda")))
      .finally(() => setCarregando(false));
  }, [id]);

  async function handleCancelar() {
    if (!id) return;
    if (!confirmandoCancelamento) {
      setConfirmandoCancelamento(true);
      return;
    }
    setCancelando(true);
    try {
      await vendaApi.cancelar(id);
      navigate("/vendas/historico", { replace: true });
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra cancelar essa venda"));
      setCancelando(false);
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-2xl px-6 py-12">
        <BackLink to="/vendas/historico">Voltar pro histórico</BackLink>

        {erro && (
          <div className="mt-6">
            <Alert tipo="erro">{erro}</Alert>
          </div>
        )}

        {carregando ? (
          <div className="mt-10 flex justify-center">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-ink-100 border-t-ink" />
          </div>
        ) : (
          venda && (
            <>
              <div className="mt-4 flex items-start justify-between gap-4">
                <div>
                  <h1 className="font-display text-2xl font-semibold text-ink">
                    Venda de {formatarData(venda.dataVenda)}
                  </h1>
                  <p className="mt-1 text-sm text-ink-400">
                    {venda.clienteNome || "Consumidor final"} · {ROTULOS_FORMA_PAGAMENTO[venda.formaPagamento]}
                  </p>
                </div>
                <span
                  className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-medium ${
                    venda.status === "CANCELADA" ? "bg-ink-50 text-ink-400" : "bg-sage/10 text-sage"
                  }`}
                >
                  {venda.status === "CANCELADA" ? "Cancelada" : "Ativa"}
                </span>
              </div>

              <div className="mt-6 overflow-hidden rounded-xl border border-ink-100 bg-white shadow-card">
                <table className="w-full text-left text-sm">
                  <thead>
                    <tr className="border-b border-ink-100 text-ink-400">
                      <th className="px-4 py-3 font-medium">Produto</th>
                      <th className="px-4 py-3 font-medium text-right">Qtd.</th>
                      <th className="px-4 py-3 font-medium text-right">Preço un.</th>
                      <th className="px-4 py-3 font-medium text-right">Subtotal</th>
                    </tr>
                  </thead>
                  <tbody>
                    {venda.itens.map((item, i) => (
                      <tr key={i} className="border-b border-ink-100 last:border-0">
                        <td className="px-4 py-3 text-ink">{item.nomeProduto}</td>
                        <td className="px-4 py-3 text-right text-ink-400">{item.quantidade}</td>
                        <td className="px-4 py-3 text-right text-ink-400">{formatarMoeda(item.precoUnitario)}</td>
                        <td className="px-4 py-3 text-right font-medium text-ink">{formatarMoeda(item.subtotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                <div className="border-t border-ink-100 px-4 py-3">
                  {venda.desconto > 0 && (
                    <div className="flex justify-between text-sm text-ink-400">
                      <span>Desconto</span>
                      <span>− {formatarMoeda(venda.desconto)}</span>
                    </div>
                  )}
                  <div className="flex justify-between font-display text-base font-semibold text-ink">
                    <span>Total</span>
                    <span>{formatarMoeda(venda.valorTotal)}</span>
                  </div>
                </div>
              </div>

              {venda.observacoes && (
                <p className="mt-4 text-sm text-ink-400">
                  <strong className="text-ink">Observações:</strong> {venda.observacoes}
                </p>
              )}

              {venda.status === "ATIVA" && (
                <button
                  onClick={handleCancelar}
                  disabled={cancelando}
                  className={`mt-6 text-sm font-medium disabled:opacity-50 ${
                    confirmandoCancelamento ? "text-rust" : "text-ink-400 hover:text-rust"
                  }`}
                >
                  {confirmandoCancelamento ? "Confirmar cancelamento?" : "Cancelar essa venda"}
                </button>
              )}
            </>
          )
        )}
      </main>
    </div>
  );
}
