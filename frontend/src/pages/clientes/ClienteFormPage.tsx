import { useEffect, useState, type FormEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { BackLink } from "@/components/ui/BackLink";
import { clienteApi } from "@/api/clientes";
import { extrairMensagemErro } from "@/api/errors";

export function ClienteFormPage() {
  const { id } = useParams();
  const editando = !!id;
  const navigate = useNavigate();

  const [nome, setNome] = useState("");
  const [telefone, setTelefone] = useState("");
  const [email, setEmail] = useState("");
  const [cpfCnpj, setCpfCnpj] = useState("");
  const [observacoes, setObservacoes] = useState("");

  const [carregandoDados, setCarregandoDados] = useState(editando);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    clienteApi
      .buscar(id)
      .then((cliente) => {
        setNome(cliente.nome);
        setTelefone(cliente.telefone ?? "");
        setEmail(cliente.email ?? "");
        setCpfCnpj(cliente.cpfCnpj ?? "");
        setObservacoes(cliente.observacoes ?? "");
      })
      .catch((err) => setErro(extrairMensagemErro(err, "Não deu pra carregar esse cliente")))
      .finally(() => setCarregandoDados(false));
  }, [id]);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);
    setSalvando(true);

    const payload = {
      nome,
      telefone: telefone || undefined,
      email: email || undefined,
      cpfCnpj: cpfCnpj || undefined,
      observacoes: observacoes || undefined,
    };

    try {
      if (editando && id) {
        await clienteApi.atualizar(id, payload);
      } else {
        await clienteApi.criar(payload);
      }
      navigate("/clientes", { replace: true });
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra salvar esse cliente"));
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-lg px-6 py-12">
        <BackLink to="/clientes">Voltar pra clientes</BackLink>

        <h1 className="mt-3 font-display text-2xl font-semibold text-ink">
          {editando ? "Editar cliente" : "Novo cliente"}
        </h1>

        {carregandoDados ? (
          <div className="mt-10 flex justify-center">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-ink-100 border-t-ink" />
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-4">
            {erro && <Alert tipo="erro">{erro}</Alert>}

            <Input label="Nome" name="nome" value={nome} onChange={(e) => setNome(e.target.value)} required />

            <Input label="Telefone" name="telefone" value={telefone} onChange={(e) => setTelefone(e.target.value)} />

            <Input
              label="Email"
              type="email"
              name="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />

            <Input label="CPF ou CNPJ" name="cpfCnpj" value={cpfCnpj} onChange={(e) => setCpfCnpj(e.target.value)} />

            <div className="flex flex-col gap-1.5">
              <label htmlFor="observacoes" className="text-sm font-medium text-ink-600">
                Observações
              </label>
              <textarea
                id="observacoes"
                value={observacoes}
                onChange={(e) => setObservacoes(e.target.value)}
                rows={3}
                className="rounded-lg border border-ink-100 bg-white px-3.5 py-2.5 text-sm placeholder:text-ink-400/60
                  focus:outline-none focus:ring-2 focus:ring-brass/40"
              />
            </div>

            <div className="mt-2 flex gap-3">
              <Button type="submit" carregando={salvando}>
                {editando ? "Salvar alterações" : "Cadastrar cliente"}
              </Button>
            </div>
          </form>
        )}
      </main>
    </div>
  );
}
