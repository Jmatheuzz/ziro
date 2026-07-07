import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { BackLink } from "@/components/ui/BackLink";
import { authApi } from "@/api/auth";
import { extrairMensagemErro } from "@/api/errors";

export function RegisterPage() {
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);
  const [enviado, setEnviado] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);

    if (senha.length < 8) {
      setErro("A senha precisa ter no mínimo 8 caracteres");
      return;
    }

    setCarregando(true);
    try {
      await authApi.registrar({ nome, email, senha });
      setEnviado(true);
    } catch (err) {
      setErro(extrairMensagemErro(err, "Não deu pra criar sua conta agora"));
    } finally {
      setCarregando(false);
    }
  }

  if (enviado) {
    return (
      <AuthLayout titulo="Quase lá">
        <Alert tipo="sucesso">
          Conta criada! Manda um oi pra <strong>{email}</strong> e clica no link de confirmação
          que a gente te mandou pra ativar o acesso.
        </Alert>
        <div className="mt-6 flex justify-center">
          <BackLink to="/login" variant="auth">Voltar pro login</BackLink>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout titulo="Criar conta" subtitulo="Leva menos de um minuto">
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {erro && <Alert tipo="erro">{erro}</Alert>}

        <Input
          label="Nome"
          name="nome"
          autoComplete="name"
          value={nome}
          onChange={(e) => setNome(e.target.value)}
          required
        />

        <Input
          label="Email"
          type="email"
          name="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <Input
          label="Senha"
          type="password"
          name="senha"
          autoComplete="new-password"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
          minLength={8}
          required
        />

        <Button type="submit" carregando={carregando}>
          Criar conta
        </Button>

        <p className="text-center text-sm text-ink-400">
          Já tem conta?{" "}
          <Link to="/login" className="font-medium text-ink hover:text-brass-700">
            Entrar
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
}
