import { useEffect, useState, type FormEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { BackLink } from "@/components/ui/BackLink";
import { contaPagarApi } from "@/api/financeiro";
import { extrairMensagemErro } from "@/api/errors";
import { hojeISO } from "@/utils/formatters";

export function ContaPagarFormPage() {
  const { id } = useParams();
  const editando = !!id;
  const navigate = useNavigate();

  const [descricao, setDescricao] = useState("");
  const [valor, setValor] = useState("");
  const [dataVencimento, setDataVencimento] = useState(hojeISO());
  const [fornecedor, setFornecedor] = useState("");
  const [categoria, setCategoria] = useState("");

  const [carregandoDados, setCarregandoDados] = useState(editando);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    contaPagarApi
      .buscar(id)
      .then((conta) => {
        setDescricao(conta.descricao);
        setValor(String(conta.valor));
        setDataVencimento(conta.dataVencimento);
        setFornecedor(conta.fornecedor ?? "");
        setCategoria(conta.categoria ?? "");
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
      fornecedor: fornecedor || undefined,
      categoria: categoria || undefined,
    };

    try {
      if (editando && id) {
        await contaPagarApi.atualizar(id, payload);
      } else {
        await contaPagarApi.criar(payload);
      }
      navigate("/financeiro/contas-a-pagar", { replace: true });
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
        <BackLink to="/financeiro/contas-a-pagar">Voltar pra contas a pagar</BackLink>

        <h1 className="mt-3 font-display text-2xl font-semibold text-ink">
          {editando ? "Editar conta a pagar" : "Nova conta a pagar"}
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

            <Input label="Fornecedor" name="fornecedor" value={fornecedor} onChange={(e) => setFornecedor(e.target.value)} />

            <Input label="Categoria" name="categoria" value={categoria} onChange={(e) => setCategoria(e.target.value)} />

            <Button type="submit" carregando={salvando} className="mt-2">
              {editando ? "Salvar alterações" : "Cadastrar conta"}
            </Button>
          </form>
        )}
      </main>
    </div>
  );
}
