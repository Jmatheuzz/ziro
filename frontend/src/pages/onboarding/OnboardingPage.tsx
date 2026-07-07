import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { ZiroMark } from "@/components/auth/ZiroMark";
import { SeletorSegmento } from "@/components/empresa/SeletorSegmento";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { empresaApi } from "@/api/empresa";
import { extrairMensagemErro } from "@/api/errors";
import { useAuth } from "@/context/AuthContext";
import type { SegmentoNegocio } from "@/types/empresa";

export function OnboardingPage() {
  const navigate = useNavigate();
  const { recarregarUsuario } = useAuth();

  const [nomeFantasia, setNomeFantasia] = useState("");
  const [razaoSocial, setRazaoSocial] = useState("");
  const [cnpjCpf, setCnpjCpf] = useState("");
  const [segmento, setSegmento] = useState<SegmentoNegocio | null>(null);

  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);

    if (!segmento) {
      setErro("Escolhe o segmento que mais parece com o seu negócio");
      return;
    }

    setCarregando(true);
    try {
      await empresaApi.criar({
        nomeFantasia,
        razaoSocial: razaoSocial || undefined,
        cnpjCpf: cnpjCpf || undefined,
        segmento,
      });
      await recarregarUsuario();
      navigate("/", { replace: true });
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra criar sua empresa agora"));
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-paper px-6 py-12">
      <div className="w-full max-w-md">
        <div className="mb-6 flex justify-center">
          <ZiroMark size={48} className="text-ink" />
        </div>

        <h1 className="text-center font-display text-2xl font-semibold text-ink">
          Vamos configurar sua empresa
        </h1>
        <p className="mt-2 text-center text-sm text-ink-400">
          Só o essencial agora — o resto você personaliza depois, sem complicação.
        </p>

        <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-4">
          {erro && <Alert tipo="erro">{erro}</Alert>}

          <Input
            label="Nome fantasia"
            name="nomeFantasia"
            placeholder="Como seu negócio é conhecido"
            value={nomeFantasia}
            onChange={(e) => setNomeFantasia(e.target.value)}
            required
          />

          <Input
            label="Razão social (opcional)"
            name="razaoSocial"
            value={razaoSocial}
            onChange={(e) => setRazaoSocial(e.target.value)}
          />

          <Input
            label="CNPJ ou CPF (opcional)"
            name="cnpjCpf"
            value={cnpjCpf}
            onChange={(e) => setCnpjCpf(e.target.value)}
          />

          <div>
            <span className="mb-1.5 block text-sm font-medium text-ink-600">
              Qual desses mais parece com o seu negócio?
            </span>
            <SeletorSegmento valor={segmento} onSelecionar={setSegmento} />
          </div>

          <Button type="submit" carregando={carregando} className="mt-2">
            Começar a usar o Ziro
          </Button>
        </form>
      </div>
    </div>
  );
}
