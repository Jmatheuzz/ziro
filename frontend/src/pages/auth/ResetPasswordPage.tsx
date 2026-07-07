import { useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { authApi } from "@/api/auth";
import { extrairMensagemErro } from "@/api/errors";

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const codigo = searchParams.get("codigo");

  const [novaSenha, setNovaSenha] = useState("");
  const [confirmacao, setConfirmacao] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);
  const [concluido, setConcluido] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);

    if (!codigo) {
      setErro("Link incompleto. Solicite a recuperação de senha de novo.");
      return;
    }
    if (novaSenha.length < 8) {
      setErro("A senha precisa ter no mínimo 8 caracteres");
      return;
    }
    if (novaSenha !== confirmacao) {
      setErro("As senhas não coincidem");
      return;
    }

    setCarregando(true);
    try {
      await authApi.redefinirSenha(codigo, novaSenha);
      setConcluido(true);
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra redefinir sua senha"));
    } finally {
      setCarregando(false);
    }
  }

  if (concluido) {
    return (
      <AuthLayout titulo="Senha redefinida">
        <Alert tipo="sucesso">Sua senha foi atualizada. Já pode entrar com ela.</Alert>
        <Link
          to="/login"
          className="mt-6 block w-full rounded-lg bg-ink px-4 py-2.5 text-center text-sm font-medium text-paper transition-standard hover:bg-ink-600"
        >
          Ir pro login
        </Link>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout titulo="Criar nova senha">
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {erro && <Alert tipo="erro">{erro}</Alert>}

        <Input
          label="Nova senha"
          type="password"
          name="novaSenha"
          autoComplete="new-password"
          value={novaSenha}
          onChange={(e) => setNovaSenha(e.target.value)}
          minLength={8}
          required
        />

        <Input
          label="Confirmar nova senha"
          type="password"
          name="confirmacao"
          autoComplete="new-password"
          value={confirmacao}
          onChange={(e) => setConfirmacao(e.target.value)}
          minLength={8}
          required
        />

        <Button type="submit" carregando={carregando}>
          Redefinir senha
        </Button>
      </form>
    </AuthLayout>
  );
}
