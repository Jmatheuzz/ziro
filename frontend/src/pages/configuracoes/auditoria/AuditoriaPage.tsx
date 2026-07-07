import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import { AppHeader } from "@/components/layout/AppHeader";
import { Alert } from "@/components/ui/Alert";
import { auditoriaApi } from "@/api/auditoria";
import { extrairMensagemErro } from "@/api/errors";
import type { RegistroAuditoriaResponse } from "@/types/auditoria";
import type { PaginaResponse } from "@/types/cliente";

const TAMANHO_PAGINA = 30;

const ENTIDADES = [
  { valor: "", rotulo: "Tudo" },
  { valor: "CLIENTE", rotulo: "Clientes" },
  { valor: "PRODUTO", rotulo: "Produtos" },
  { valor: "CATEGORIA", rotulo: "Categorias" },
  { valor: "CONTA_PAGAR", rotulo: "Contas a pagar" },
  { valor: "CONTA_RECEBER", rotulo: "Contas a receber" },
  { valor: "MOVIMENTACAO_CAIXA", rotulo: "Fluxo de caixa" },
  { valor: "MODULO", rotulo: "Módulos" },
  { valor: "EMPRESA", rotulo: "Empresa" },
  { valor: "USUARIO", rotulo: "Usuário" },
];

function formatarDataHora(iso: string): string {
  return new Date(iso).toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function AuditoriaPage() {
  const [entidade, setEntidade] = useState("");
  const [pagina, setPagina] = useState(0);
  const [dados, setDados] = useState<PaginaResponse<RegistroAuditoriaResponse> | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [semPermissao, setSemPermissao] = useState(false);

  useEffect(() => {
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [entidade, pagina]);

  async function carregar() {
    setCarregando(true);
    setErro(null);
    setSemPermissao(false);
    try {
      const resposta = await auditoriaApi.listar({
        entidade: entidade || undefined,
        page: pagina,
        size: TAMANHO_PAGINA,
      });
      setDados(resposta);
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 403) setSemPermissao(true);
      setErro(extrairMensagemErro(err, "Não deu pra carregar o histórico"));
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-3xl px-6 py-12">
        <h1 className="font-display text-2xl font-semibold text-ink">Histórico de ações</h1>
        <p className="mt-2 text-sm text-ink-400">Tudo que foi criado, alterado ou removido no sistema.</p>

        {erro && (
          <div className="mt-6">
            <Alert tipo="erro">{erro}</Alert>
          </div>
        )}

        {!semPermissao && (
          <div className="mt-6 flex flex-wrap gap-1">
            {ENTIDADES.map((e) => (
              <button
                key={e.valor}
                onClick={() => {
                  setEntidade(e.valor);
                  setPagina(0);
                }}
                className={`rounded-md px-3 py-1.5 text-sm font-medium transition-standard ${
                  entidade === e.valor ? "bg-ink-50 text-ink" : "text-ink-400 hover:text-ink"
                }`}
              >
                {e.rotulo}
              </button>
            ))}
          </div>
        )}

        {carregando ? (
          <div className="mt-10 flex justify-center">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-ink-100 border-t-ink" />
          </div>
        ) : (
          !semPermissao &&
          dados && (
            <>
              {dados.content.length === 0 ? (
                <p className="mt-10 text-center text-sm text-ink-400">Nenhum registro ainda.</p>
              ) : (
                <div className="mt-4 flex flex-col gap-2">
                  {dados.content.map((registro) => (
                    <div key={registro.id} className="rounded-lg border border-ink-100 bg-white px-4 py-3">
                      <div className="flex items-start justify-between gap-4">
                        <p className="text-sm text-ink">{registro.descricao}</p>
                        <span className="shrink-0 text-xs text-ink-400">{formatarDataHora(registro.criadoEm)}</span>
                      </div>
                      <p className="mt-1 text-xs text-ink-400">
                        {registro.usuarioNome ?? "Sistema"} · {registro.entidade.replaceAll("_", " ").toLowerCase()}
                      </p>
                    </div>
                  ))}
                </div>
              )}

              {dados.totalPages > 1 && (
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
          )
        )}
      </main>
    </div>
  );
}
