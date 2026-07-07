import { useEffect, useState } from "react";
import { AppHeader } from "@/components/layout/AppHeader";
import { Switch } from "@/components/ui/Switch";
import { Alert } from "@/components/ui/Alert";
import { moduloApi } from "@/api/modulos";
import { extrairMensagemErro } from "@/api/errors";
import { useModulos } from "@/context/ModulosContext";
import type { ModuloConfiguracaoResponse, TipoModulo } from "@/types/empresa";

const METADADOS_MODULO: Record<TipoModulo, { titulo: string; descricao: string }> = {
  FINANCEIRO: {
    titulo: "Financeiro",
    descricao: "Contas a pagar, a receber e fluxo de caixa",
  },
  ESTOQUE: {
    titulo: "Estoque",
    descricao: "Controle de quantidade e alerta de estoque baixo",
  },
  CLIENTES: {
    titulo: "Clientes",
    descricao: "Cadastro e histórico dos seus clientes",
  },
  VENDAS: {
    titulo: "Vendas",
    descricao: "Registro de pedidos e vendas do dia a dia",
  },
};

interface ConfigEstoque {
  alertaEstoqueBaixo: boolean;
  estoqueMinimoPadrao: number;
}

function parseConfigEstoque(json: string | null): ConfigEstoque {
  const padrao: ConfigEstoque = { alertaEstoqueBaixo: true, estoqueMinimoPadrao: 5 };
  if (!json) return padrao;
  try {
    return { ...padrao, ...JSON.parse(json) };
  } catch {
    return padrao;
  }
}

export function PersonalizacaoPage() {
  const { recarregarModulos } = useModulos();
  const [modulos, setModulos] = useState<ModuloConfiguracaoResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [salvandoModulo, setSalvandoModulo] = useState<TipoModulo | null>(null);

  useEffect(() => {
    moduloApi
      .listar()
      .then(setModulos)
      .catch((err) => setErro(extrairMensagemErro(err, "Não deu pra carregar os módulos")))
      .finally(() => setCarregando(false));
  }, []);

  async function alternarModulo(modulo: TipoModulo, ativo: boolean) {
    setErro(null);
    setSalvandoModulo(modulo);

    const anterior = modulos;
    setModulos((atuais) => atuais.map((m) => (m.modulo === modulo ? { ...m, ativo } : m)));

    try {
      const atualizado = await moduloApi.atualizar(modulo, { ativo });
      setModulos((atuais) => atuais.map((m) => (m.modulo === modulo ? atualizado : m)));
      recarregarModulos();
    } catch (err) {
      setModulos(anterior); // desfaz a mudanca otimista se a API recusar
      setErro(extrairMensagemErro(err, "Não deu pra atualizar esse módulo"));
    } finally {
      setSalvandoModulo(null);
    }
  }

  async function atualizarConfigEstoque(modulo: TipoModulo, config: ConfigEstoque) {
    setErro(null);
    setSalvandoModulo(modulo);
    try {
      const atualizado = await moduloApi.atualizar(modulo, {
        configuracaoJson: JSON.stringify(config),
      });
      setModulos((atuais) => atuais.map((m) => (m.modulo === modulo ? atualizado : m)));
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra salvar essa configuração"));
    } finally {
      setSalvandoModulo(null);
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-2xl px-6 py-12">
        <h1 className="font-display text-2xl font-semibold text-ink">Personalização</h1>
        <p className="mt-2 text-sm text-ink-400">
          Liga só o que faz sentido pro seu negócio. Você pode mudar isso a qualquer momento.
        </p>

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
          <div className="mt-8 flex flex-col gap-3">
            {modulos.map((config) => {
              const meta = METADADOS_MODULO[config.modulo];
              const salvando = salvandoModulo === config.modulo;

              return (
                <div
                  key={config.modulo}
                  className="rounded-xl border border-ink-100 bg-white p-4 shadow-card"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <h2 className="font-display text-base font-semibold text-ink">{meta.titulo}</h2>
                      <p className="mt-0.5 text-sm text-ink-400">{meta.descricao}</p>
                    </div>
                    <Switch
                      ativo={config.ativo}
                      disabled={salvando}
                      rotulo={`Ativar módulo ${meta.titulo}`}
                      onChange={(novoValor) => alternarModulo(config.modulo, novoValor)}
                    />
                  </div>

                  {config.modulo === "ESTOQUE" && config.ativo && (
                    <ConfiguracaoEstoque
                      configuracaoJson={config.configuracaoJson}
                      salvando={salvando}
                      onSalvar={(cfg) => atualizarConfigEstoque("ESTOQUE", cfg)}
                    />
                  )}
                </div>
              );
            })}
          </div>
        )}
      </main>
    </div>
  );
}

function ConfiguracaoEstoque({
  configuracaoJson,
  salvando,
  onSalvar,
}: {
  configuracaoJson: string | null;
  salvando: boolean;
  onSalvar: (config: ConfigEstoque) => void;
}) {
  const config = parseConfigEstoque(configuracaoJson);
  const [estoqueMinimo, setEstoqueMinimo] = useState(config.estoqueMinimoPadrao);

  return (
    <div className="mt-4 flex flex-col gap-3 border-t border-ink-100 pt-4">
      <div className="flex items-center justify-between">
        <span className="text-sm text-ink-600">Avisar quando o estoque ficar baixo</span>
        <Switch
          ativo={config.alertaEstoqueBaixo}
          disabled={salvando}
          rotulo="Avisar quando o estoque ficar baixo"
          onChange={(novoValor) => onSalvar({ ...config, alertaEstoqueBaixo: novoValor })}
        />
      </div>

      {config.alertaEstoqueBaixo && (
        <label className="flex items-center justify-between text-sm text-ink-600">
          Quantidade mínima padrão
          <input
            type="number"
            min={0}
            value={estoqueMinimo}
            disabled={salvando}
            onChange={(e) => setEstoqueMinimo(Number(e.target.value))}
            onBlur={() => onSalvar({ ...config, estoqueMinimoPadrao: estoqueMinimo })}
            className="w-20 rounded-md border border-ink-100 px-2 py-1 text-right text-sm focus:outline-none focus:ring-2 focus:ring-brass/40"
          />
        </label>
      )}
    </div>
  );
}
