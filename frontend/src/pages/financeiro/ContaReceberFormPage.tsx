import { useEffect, useState, type FormEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { BackLink } from "@/components/ui/BackLink";
import { contaReceberApi } from "@/api/financeiro";
import { clienteApi } from "@/api/clientes";
import { extrairMensagemErro } from "@/api/errors";
import { hojeISO } from "@/utils/formatters";
import type { ClienteResponse } from "@/types/cliente";

export function ContaReceberFormPage() {
  const { id } = useParams();
  const editando = !!id;
  const navigate = useNavigate();

  const [descricao, setDescricao] = useState("");
  const [valor, setValor] = useState("");
  const [dataVencimento, setDataVencimento] = useState(hojeISO());
  const [clienteId, setClienteId] = useState("");
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);

  const [carregandoDados, setCarregandoDados] = useState(editando);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    // lista de clientes pro seletor - se o modulo Clientes estiver desativado, so nao mostra o seletor
    clienteApi
      .listar({ size: 100 })
      .then((pagina) => setClientes(pagina.content))
      .catch(() => setClientes([]));
  }, []);

  useEffect(() => {
    if (!id) return;
    contaReceberApi
      .buscar(id)
      .then((conta) => {
        setDescricao(conta.descricao);
        setValor(String(conta.valor));
        setDataVencimento(conta.dataVencimento);
        setClienteId(conta.clienteId ?? "");
      })
      .catch((err) => setErro(extrairMensagemErro(err, "Não deu pra carregar essa conta")))
      .finally(() => setCarregandoDados(false));
  }, [id]);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);

    const valorNumerico = Number(valor.replace(",", "."));
    if (!valorNumerico || valorNumerico <= 0) {
      setErro("Informe um valor válido, maior que zero");
      return;
    }

    setSalvando(true);
    const payload = {
      descricao,
      valor: valorNumerico,
      dataVencimento,
      clienteId: clienteId || undefined,
    };

    try {
      if (editando && id) {
        await contaReceberApi.atualizar(id, payload);
      } else {
        await contaReceberApi.criar(payload);
      }
      navigate("/financeiro/contas-a-receber", { replace: true });
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra salvar essa conta"));
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-lg px-6 py-12">
        <BackLink to="/financeiro/contas-a-receber">Voltar pra contas a receber</BackLink>

        <h1 className="mt-3 font-display text-2xl font-semibold text-ink">
          {editando ? "Editar conta a receber" : "Nova conta a receber"}
        </h1>

        {carregandoDados ? (
          <div className="mt-10 flex justify-center">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-ink-100 border-t-ink" />
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-4">
            {erro && <Alert tipo="erro">{erro}</Alert>}

            <Input label="Descrição" name="descricao" value={descricao} onChange={(e) => setDescricao(e.target.value)} required />

            <Input
              label="Valor (R$)"
              name="valor"
              inputMode="decimal"
              value={valor}
              onChange={(e) => setValor(e.target.value)}
              placeholder="0,00"
              required
            />

            <Input
              label="Data de vencimento"
              type="date"
              name="dataVencimento"
              value={dataVencimento}
              onChange={(e) => setDataVencimento(e.target.value)}
              required
            />

            {clientes.length > 0 && (
              <div className="flex flex-col gap-1.5">
                <label htmlFor="cliente" className="text-sm font-medium text-ink-600">
                  Cliente (opcional)
                </label>
                <select
                  id="cliente"
                  value={clienteId}
                  onChange={(e) => setClienteId(e.target.value)}
                  className="rounded-lg border border-ink-100 bg-white px-3.5 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brass/40"
                >
                  <option value="">Nenhum</option>
                  {clientes.map((cliente) => (
                    <option key={cliente.id} value={cliente.id}>
                      {cliente.nome}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <Button type="submit" carregando={salvando} className="mt-2">
              {editando ? "Salvar alterações" : "Cadastrar conta"}
            </Button>
          </form>
        )}
      </main>
    </div>
  );
}
