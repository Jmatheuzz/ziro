import { useState, type FormEvent } from "react";

import { AuthLayout } from "@/components/auth/AuthLayout";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { BackLink } from "@/components/ui/BackLink";
import { authApi } from "@/api/auth";
import { extrairMensagemErro } from "@/api/errors";

export function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);
  const [enviado, setEnviado] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);
    setCarregando(true);

    try {
      await authApi.esqueciSenha(email);
      setEnviado(true);
    } catch (err) {
      setErro(extrairMensagemErro(err));
    } finally {
      setCarregando(false);
    }
  }

  if (enviado) {
    return (
      <AuthLayout titulo="Confira seu email">
        <Alert tipo="sucesso">
          Se <strong>{email}</strong> estiver cadastrado, você recebe um link pra criar uma senha nova
          em instantes.
        </Alert>
        <div className="mt-6 flex justify-center">
          <BackLink to="/login" variant="auth">Voltar pro login</BackLink>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout titulo="Esqueceu a senha?" subtitulo="A gente te manda um link pra criar uma nova">
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {erro && <Alert tipo="erro">{erro}</Alert>}

        <Input
          label="Email"
          type="email"
          name="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <Button type="submit" carregando={carregando}>
          Enviar link
        </Button>

        <p className="text-center text-sm text-ink-400">
          <BackLink to="/login" variant="auth">Voltar pro login</BackLink>
        </p>
      </form>
    </AuthLayout>
  );
}
