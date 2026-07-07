import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Alert } from "@/components/ui/Alert";
import { BackLink } from "@/components/ui/BackLink";
import { authApi } from "@/api/auth";
import { extrairMensagemErro } from "@/api/errors";

type Estado = "verificando" | "sucesso" | "erro";

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const codigo = searchParams.get("codigo");

  const [estado, setEstado] = useState<Estado>("verificando");
  const [mensagem, setMensagem] = useState("");

  useEffect(() => {
    if (!codigo) {
      setEstado("erro");
      setMensagem("Link de verificação incompleto. Confira se copiou o link inteiro do email.");
      return;
    }

    authApi
      .verificarEmail(codigo)
      .then(() => setEstado("sucesso"))
      .catch((err) => {
        setEstado("erro");
        setMensagem(extrairMensagemErro(err, "Não deu pra verificar seu email"));
      });
  }, [codigo]);

  return (
    <AuthLayout titulo="Verificação de email">
      {estado === "verificando" && (
        <div className="flex items-center gap-3 text-sm text-ink-400">
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-ink-100 border-t-ink" />
          Confirmando seu email...
        </div>
      )}

      {estado === "sucesso" && (
        <div className="flex flex-col gap-4">
          <Alert tipo="sucesso">Email confirmado! Sua conta já está ativa.</Alert>
          <Link
            to="/login"
            className="w-full rounded-lg bg-ink px-4 py-2.5 text-center text-sm font-medium text-paper transition-standard hover:bg-ink-600"
          >
            Ir pro login
          </Link>
        </div>
      )}

      {estado === "erro" && (
        <div className="flex flex-col gap-4">
          <Alert tipo="erro">{mensagem}</Alert>
          <BackLink to="/login" variant="auth">Voltar pro login</BackLink>
        </div>
      )}
    </AuthLayout>
  );
}
