import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { authApi } from "@/api/auth";
import { tokenStorage } from "@/api/tokenStorage";
import type { UsuarioAutenticado } from "@/types/auth";

interface AuthContextValor {
  usuario: UsuarioAutenticado | null;
  carregando: boolean;
  login: (accessToken: string, refreshToken: string) => Promise<void>;
  logout: () => Promise<void>;
  recarregarUsuario: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValor | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioAutenticado | null>(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    async function carregarSessao() {
      if (!tokenStorage.getAccessToken()) {
        setCarregando(false);
        return;
      }
      try {
        const dados = await authApi.me();
        setUsuario(dados);
      } catch {
        tokenStorage.limpar();
        setUsuario(null);
      } finally {
        setCarregando(false);
      }
    }
    carregarSessao();
  }, []);

  async function login(accessToken: string, refreshToken: string) {
    tokenStorage.salvar(accessToken, refreshToken);
    const dados = await authApi.me();
    setUsuario(dados);
  }

  async function logout() {
    const refreshToken = tokenStorage.getRefreshToken();
    tokenStorage.limpar();
    setUsuario(null);
    if (refreshToken) {
      try {
        await authApi.logout(refreshToken);
      } catch {
        // se a chamada de logout falhar no servidor, tudo bem - o token local ja foi limpo
      }
    }
  }

  async function recarregarUsuario() {
    const dados = await authApi.me();
    setUsuario(dados);
  }

  return (
    <AuthContext.Provider value={{ usuario, carregando, login, logout, recarregarUsuario }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValor {
  const contexto = useContext(AuthContext);
  if (!contexto) {
    throw new Error("useAuth precisa ser usado dentro de um AuthProvider");
  }
  return contexto;
}
