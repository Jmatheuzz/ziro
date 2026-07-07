import { useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import { AppHeader } from "@/components/layout/AppHeader";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { BackLink } from "@/components/ui/BackLink";
import { fluxoCaixaApi } from "@/api/financeiro";
import { extrairMensagemErro } from "@/api/errors";
import { formatarData, formatarMoeda, hojeISO } from "@/utils/formatters";
import type { FluxoCaixaResponse, TipoMovimentacao } from "@/types/financeiro";

export function FluxoCaixaPage() {
  const [fluxo, setFluxo] = useState<FluxoCaixaResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [moduloInativo, setModuloInativo] = useState(false);
  const [mostrarForm, setMostrarForm] = useState(false);

  const [tipo, setTipo] = useState<TipoMovimentacao>("ENTRADA");
  const [valor, setValor] = useState("");
  const [descricao, setDescricao] = useState("");
  const [data, setData] = useState(hojeISO());
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    carregar();
  }, []);

  async function carregar() {
    setCarregando(true);
    setErro(null);
    setModuloInativo(false);
    try {
      const resposta = await fluxoCaixaApi.listar();
      setFluxo(resposta);
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 403) setModuloInativo(true);
      setErro(extrairMensagemErro(err, "Não deu pra carregar o fluxo de caixa"));
    } finally {
      setCarregando(false);
    }
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);

    const valorNumerico = Number(valor.replace(",", "."));
    if (!valorNumerico || valorNumerico <= 0) {
      setErro("Informe um valor válido, maior que zero");
      return;
    }

    setSalvando(true);
    try {
      await fluxoCaixaApi.lancarManual({ tipo, valor: valorNumerico, descricao, data });
      setValor("");
      setDescricao("");
      setMostrarForm(false);
      carregar();
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra registrar esse lançamento"));
    } finally {
      setSalvando(false);
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

      <main className="mx-auto max-w-3xl px-6 py-12">
        <BackLink to="/financeiro">Financeiro</BackLink>

        <div className="mt-3 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="font-display text-2xl font-semibold text-ink">Fluxo de caixa</h1>
            <p className="mt-1 text-sm text-ink-400">Movimentações do mês atual</p>
          </div>
          <Button className="w-auto px-4" onClick={() => setMostrarForm((v) => !v)}>
            {mostrarForm ? "Cancelar" : "Novo lançamento"}
          </Button>
        </div>

        {erro && !moduloInativo && (
          <div className="mt-4">
            <Alert tipo="erro">{erro}</Alert>
          </div>
        )}

        {mostrarForm && (
          <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-4 rounded-xl border border-ink-100 bg-white p-5 shadow-card">
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

            <Input label="Descrição" name="descricao" value={descricao} onChange={(e) => setDescricao(e.target.value)} required />

            <div className="grid grid-cols-2 gap-4">
              <Input label="Valor (R$)" name="valor" inputMode="decimal" value={valor} onChange={(e) => setValor(e.target.value)} placeholder="0,00" required />
              <Input label="Data" type="date" name="data" value={data} onChange={(e) => setData(e.target.value)} required />
            </div>

            <Button type="submit" carregando={salvando}>
              Registrar lançamento
            </Button>
          </form>
        )}

        {carregando ? (
          <div className="mt-10 flex justify-center">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-ink-100 border-t-ink" />
          </div>
        ) : (
          fluxo && (
            <>
              <div className="mt-6 grid grid-cols-3 gap-4">
                <div className="rounded-xl border border-ink-100 bg-white p-4 shadow-card">
                  <p className="text-xs font-medium text-ink-400">Entradas</p>
                  <p className="mt-1 font-display text-lg font-semibold text-sage">{formatarMoeda(fluxo.totalEntradas)}</p>
                </div>
                <div className="rounded-xl border border-ink-100 bg-white p-4 shadow-card">
                  <p className="text-xs font-medium text-ink-400">Saídas</p>
                  <p className="mt-1 font-display text-lg font-semibold text-rust">{formatarMoeda(fluxo.totalSaidas)}</p>
                </div>
                <div className="rounded-xl border border-ink-100 bg-white p-4 shadow-card">
                  <p className="text-xs font-medium text-ink-400">Saldo</p>
                  <p className={`mt-1 font-display text-lg font-semibold ${fluxo.saldo < 0 ? "text-rust" : "text-ink"}`}>
                    {formatarMoeda(fluxo.saldo)}
                  </p>
                </div>
              </div>

              {fluxo.movimentacoes.length === 0 ? (
                <p className="mt-10 text-center text-sm text-ink-400">Nenhuma movimentação nesse período ainda.</p>
              ) : (
                <div className="mt-6 overflow-hidden rounded-xl border border-ink-100 bg-white shadow-card">
                  <table className="w-full text-left text-sm">
                    <thead>
                      <tr className="border-b border-ink-100 text-ink-400">
                        <th className="px-4 py-3 font-medium">Data</th>
                        <th className="px-4 py-3 font-medium">Descrição</th>
                        <th className="px-4 py-3 font-medium text-right">Valor</th>
                      </tr>
                    </thead>
                    <tbody>
                      {fluxo.movimentacoes.map((mov) => (
                        <tr key={mov.id} className="border-b border-ink-100 last:border-0">
                          <td className="px-4 py-3 text-ink-400">{formatarData(mov.data)}</td>
                          <td className="px-4 py-3 text-ink">{mov.descricao}</td>
                          <td className={`px-4 py-3 text-right font-medium ${mov.tipo === "ENTRADA" ? "text-sage" : "text-rust"}`}>
                            {mov.tipo === "ENTRADA" ? "+" : "−"} {formatarMoeda(mov.valor)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          )
        )}
      </main>
    </div>
  );
}
