import { useEffect, useState, type FormEvent } from "react";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { SeletorModulos } from "@/components/equipe/SeletorModulos";
import { equipeApi } from "@/api/equipe";
import { moduloApi } from "@/api/modulos";
import { extrairMensagemErro } from "@/api/errors";
import type { OperadorResponse } from "@/types/equipe";
import type { TipoModulo } from "@/types/empresa";

const ROTULOS_STATUS: Record<string, { texto: string; classe: string }> = {
  ATIVO: { texto: "Ativo", classe: "bg-sage/10 text-sage" },
  PENDENTE_VERIFICACAO: { texto: "Convite pendente", classe: "bg-brass/10 text-brass-700" },
  INATIVO: { texto: "Desativado", classe: "bg-ink-50 text-ink-400" },
};

export function EquipePage() {
  const [operadores, setOperadores] = useState<OperadorResponse[]>([]);
  const [modulosDisponiveis, setModulosDisponiveis] = useState<TipoModulo[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [mostrarConvite, setMostrarConvite] = useState(false);
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [modulosConvite, setModulosConvite] = useState<TipoModulo[]>([]);
  const [enviandoConvite, setEnviandoConvite] = useState(false);

  const [operadorEditando, setOperadorEditando] = useState<string | null>(null);
  const [modulosEdicao, setModulosEdicao] = useState<TipoModulo[]>([]);
  const [salvandoPermissoes, setSalvandoPermissoes] = useState(false);
  const [confirmandoDesativacao, setConfirmandoDesativacao] = useState<string | null>(null);

  useEffect(() => {
    carregar();
  }, []);

  async function carregar() {
    setCarregando(true);
    setErro(null);
    try {
      const [listaOperadores, listaModulos] = await Promise.all([equipeApi.listar(), moduloApi.listar()]);
      setOperadores(listaOperadores);
      setModulosDisponiveis(listaModulos.filter((m) => m.ativo).map((m) => m.modulo));
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra carregar a equipe"));
    } finally {
      setCarregando(false);
    }
  }

  async function handleConvidar(e: FormEvent) {
    e.preventDefault();
    setErro(null);

    if (modulosConvite.length === 0) {
      setErro("Escolhe pelo menos um módulo pra esse operador acessar");
      return;
    }

    setEnviandoConvite(true);
    try {
      const novoOperador = await equipeApi.convidar({ nome, email, modulos: modulosConvite });
      setOperadores((atuais) => [...atuais, novoOperador]);
      setNome("");
      setEmail("");
      setModulosConvite([]);
      setMostrarConvite(false);
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra enviar esse convite"));
    } finally {
      setEnviandoConvite(false);
    }
  }

  function iniciarEdicao(operador: OperadorResponse) {
    setOperadorEditando(operador.id);
    setModulosEdicao(operador.modulos);
  }

  async function handleSalvarPermissoes(operadorId: string) {
    setSalvandoPermissoes(true);
    setErro(null);
    try {
      const atualizado = await equipeApi.atualizarPermissoes(operadorId, { modulos: modulosEdicao });
      setOperadores((atuais) => atuais.map((o) => (o.id === operadorId ? atualizado : o)));
      setOperadorEditando(null);
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra salvar as permissões"));
    } finally {
      setSalvandoPermissoes(false);
    }
  }

  async function handleDesativar(operadorId: string) {
    if (confirmandoDesativacao !== operadorId) {
      setConfirmandoDesativacao(operadorId);
      return;
    }
    setConfirmandoDesativacao(null);
    try {
      await equipeApi.desativar(operadorId);
      carregar();
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra desativar esse acesso"));
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-2xl px-6 py-12">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="font-display text-2xl font-semibold text-ink">Equipe</h1>
            <p className="mt-2 text-sm text-ink-400">Quem tem acesso ao sistema e o que cada um pode ver.</p>
          </div>
          <Button className="w-auto px-4" onClick={() => setMostrarConvite((v) => !v)}>
            {mostrarConvite ? "Cancelar" : "Convidar pessoa"}
          </Button>
        </div>

        {erro && (
          <div className="mt-6">
            <Alert tipo="erro">{erro}</Alert>
          </div>
        )}

        {mostrarConvite && (
          <form onSubmit={handleConvidar} className="mt-6 flex flex-col gap-4 rounded-xl border border-ink-100 bg-white p-5 shadow-card">
            <Input label="Nome" name="nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
            <Input label="Email" type="email" name="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

            <div>
              <span className="mb-1.5 block text-sm font-medium text-ink-600">Módulos que essa pessoa vai acessar</span>
              <SeletorModulos
                modulosDisponiveis={modulosDisponiveis}
                selecionados={modulosConvite}
                onChange={setModulosConvite}
              />
            </div>

            <Button type="submit" carregando={enviandoConvite}>
              Enviar convite
            </Button>
          </form>
        )}

        {carregando ? (
          <div className="mt-10 flex justify-center">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-ink-100 border-t-ink" />
          </div>
        ) : operadores.length === 0 ? (
          <p className="mt-10 text-center text-sm text-ink-400">
            Só você por enquanto. Convide alguém pra dividir o trabalho.
          </p>
        ) : (
          <div className="mt-6 flex flex-col gap-3">
            {operadores.map((operador) => {
              const status = ROTULOS_STATUS[operador.status] ?? ROTULOS_STATUS.ATIVO;
              const editando = operadorEditando === operador.id;

              return (
                <div key={operador.id} className="rounded-xl border border-ink-100 bg-white p-4 shadow-card">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="font-medium text-ink">{operador.nome}</p>
                      <p className="text-sm text-ink-400">{operador.email}</p>
                    </div>
                    <span className={`shrink-0 rounded-full px-2 py-0.5 text-xs font-medium ${status.classe}`}>
                      {status.texto}
                    </span>
                  </div>

                  {editando ? (
                    <div className="mt-4 flex flex-col gap-3 border-t border-ink-100 pt-4">
                      <SeletorModulos
                        modulosDisponiveis={modulosDisponiveis}
                        selecionados={modulosEdicao}
                        onChange={setModulosEdicao}
                      />
                      <div className="flex gap-3">
                        <Button
                          className="w-auto px-4"
                          carregando={salvandoPermissoes}
                          onClick={() => handleSalvarPermissoes(operador.id)}
                        >
                          Salvar
                        </Button>
                        <Button variant="ghost" className="w-auto px-4" onClick={() => setOperadorEditando(null)}>
                          Cancelar
                        </Button>
                      </div>
                    </div>
                  ) : (
                    <div className="mt-3 flex flex-wrap items-center justify-between gap-3 border-t border-ink-100 pt-3">
                      <div className="flex flex-wrap gap-1.5">
                        {operador.modulos.length === 0 ? (
                          <span className="text-xs text-ink-400">Nenhum módulo liberado</span>
                        ) : (
                          operador.modulos.map((modulo) => (
                            <span key={modulo} className="rounded-full bg-ink-50 px-2 py-0.5 text-xs text-ink-600">
                              {modulo}
                            </span>
                          ))
                        )}
                      </div>

                      {operador.status !== "INATIVO" && (
                        <div className="flex gap-3">
                          <button
                            onClick={() => iniciarEdicao(operador)}
                            className="text-xs font-medium text-ink-400 hover:text-ink"
                          >
                            Editar acessos
                          </button>
                          <button
                            onClick={() => handleDesativar(operador.id)}
                            className={`text-xs font-medium ${
                              confirmandoDesativacao === operador.id ? "text-rust" : "text-ink-400 hover:text-rust"
                            }`}
                          >
                            {confirmandoDesativacao === operador.id ? "Confirmar?" : "Desativar"}
                          </button>
                        </div>
                      )}
                    </div>
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
