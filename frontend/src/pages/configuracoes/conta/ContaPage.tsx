import { useState, type FormEvent } from "react";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { usuarioApi } from "@/api/usuario";
import { extrairMensagemErro } from "@/api/errors";
import { useAuth } from "@/context/AuthContext";

export function ContaPage() {
  const { usuario, recarregarUsuario } = useAuth();

  const [nome, setNome] = useState(usuario?.nome ?? "");
  const [salvandoPerfil, setSalvandoPerfil] = useState(false);
  const [erroPerfil, setErroPerfil] = useState<string | null>(null);
  const [sucessoPerfil, setSucessoPerfil] = useState(false);

  const [senhaAtual, setSenhaAtual] = useState("");
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmarNovaSenha, setConfirmarNovaSenha] = useState("");
  const [salvandoSenha, setSalvandoSenha] = useState(false);
  const [erroSenha, setErroSenha] = useState<string | null>(null);
  const [sucessoSenha, setSucessoSenha] = useState(false);

  async function handleSalvarPerfil(e: FormEvent) {
    e.preventDefault();
    setErroPerfil(null);
    setSucessoPerfil(false);
    setSalvandoPerfil(true);

    try {
      await usuarioApi.atualizarPerfil({ nome });
      await recarregarUsuario();
      setSucessoPerfil(true);
    } catch (err) {
      setErroPerfil(extrairMensagemErro(err, "Não deu pra salvar seu perfil"));
    } finally {
      setSalvandoPerfil(false);
    }
  }

  async function handleTrocarSenha(e: FormEvent) {
    e.preventDefault();
    setErroSenha(null);
    setSucessoSenha(false);

    if (novaSenha.length < 8) {
      setErroSenha("A nova senha precisa ter no mínimo 8 caracteres");
      return;
    }
    if (novaSenha !== confirmarNovaSenha) {
      setErroSenha("As senhas não coincidem");
      return;
    }

    setSalvandoSenha(true);
    try {
      await usuarioApi.trocarSenha({ senhaAtual, novaSenha });
      setSenhaAtual("");
      setNovaSenha("");
      setConfirmarNovaSenha("");
      setSucessoSenha(true);
    } catch (err) {
      setErroSenha(extrairMensagemErro(err, "Não deu pra trocar sua senha"));
    } finally {
      setSalvandoSenha(false);
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AppHeader />

      <main className="mx-auto max-w-lg px-6 py-12">
        <h1 className="font-display text-2xl font-semibold text-ink">Gerenciar conta</h1>

        <section className="mt-8">
          <h2 className="font-display text-base font-semibold text-ink">Perfil</h2>
          <form onSubmit={handleSalvarPerfil} className="mt-4 flex flex-col gap-4">
            {erroPerfil && <Alert tipo="erro">{erroPerfil}</Alert>}
            {sucessoPerfil && <Alert tipo="sucesso">Perfil atualizado.</Alert>}

            <Input label="Nome" name="nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
            <Input label="Email" name="email" value={usuario?.email ?? ""} disabled />

            <Button type="submit" carregando={salvandoPerfil} className="mt-1">
              Salvar perfil
            </Button>
          </form>
        </section>

        <section className="mt-10 border-t border-ink-100 pt-8">
          <h2 className="font-display text-base font-semibold text-ink">Trocar senha</h2>
          <form onSubmit={handleTrocarSenha} className="mt-4 flex flex-col gap-4">
            {erroSenha && <Alert tipo="erro">{erroSenha}</Alert>}
            {sucessoSenha && <Alert tipo="sucesso">Senha alterada com sucesso.</Alert>}

            <Input
              label="Senha atual"
              type="password"
              name="senhaAtual"
              autoComplete="current-password"
              value={senhaAtual}
              onChange={(e) => setSenhaAtual(e.target.value)}
              required
            />
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
              name="confirmarNovaSenha"
              autoComplete="new-password"
              value={confirmarNovaSenha}
              onChange={(e) => setConfirmarNovaSenha(e.target.value)}
              minLength={8}
              required
            />

            <Button type="submit" carregando={salvandoSenha} className="mt-1">
              Trocar senha
            </Button>
          </form>
        </section>
      </main>
    </div>
  );
}
