import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { authApi } from "@/api/auth";
import { extrairMensagemErro } from "@/api/errors";
import { useAuth } from "@/context/AuthContext";

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);
    setCarregando(true);

    try {
      const tokens = await authApi.login({ email, senha });
      await login(tokens.accessToken, tokens.refreshToken);
      navigate("/", { replace: true });
    } catch (err) {
      setErro(extrairMensagemErro(err, "Email ou senha inválidos"));
    } finally {
      setCarregando(false);
    }
  }

  return (
    <AuthLayout titulo="Entrar" subtitulo="Acesse a gestão do seu negócio">
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

        <Input
          label="Senha"
          type="password"
          name="senha"
          autoComplete="current-password"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
          required
        />

        <div className="-mt-1 text-right">
          <Link to="/esqueci-senha" className="text-xs font-medium text-ink-400 hover:text-ink">
            Esqueceu a senha?
          </Link>
        </div>

        <Button type="submit" carregando={carregando}>
          Entrar
        </Button>

        <p className="text-center text-sm text-ink-400">
          Ainda não tem conta?{" "}
          <Link to="/registrar" className="font-medium text-ink hover:text-brass-700">
            Criar conta
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
}
