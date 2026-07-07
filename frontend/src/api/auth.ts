import { api } from "./client";
import type {
  LoginPayload,
  MensagemResponse,
  RegisterPayload,
  TokenResponse,
  UsuarioAutenticado,
} from "@/types/auth";

export const authApi = {
  async registrar(payload: RegisterPayload): Promise<MensagemResponse> {
    const { data } = await api.post<MensagemResponse>("/api/auth/registrar", payload);
    return data;
  },

  async login(payload: LoginPayload): Promise<TokenResponse> {
    const { data } = await api.post<TokenResponse>("/api/auth/login", payload);
    return data;
  },

  async logout(refreshToken: string): Promise<void> {
    await api.post("/api/auth/logout", { refreshToken });
  },

  async verificarEmail(codigo: string): Promise<MensagemResponse> {
    const { data } = await api.post<MensagemResponse>("/api/auth/verificar-email", { codigo });
    return data;
  },

  async esqueciSenha(email: string): Promise<MensagemResponse> {
    const { data } = await api.post<MensagemResponse>("/api/auth/esqueci-senha", { email });
    return data;
  },

  async redefinirSenha(codigo: string, novaSenha: string): Promise<MensagemResponse> {
    const { data } = await api.post<MensagemResponse>("/api/auth/redefinir-senha", {
      codigo,
      novaSenha,
    });
    return data;
  },

  async me(): Promise<UsuarioAutenticado> {
    const { data } = await api.get<UsuarioAutenticado>("/api/auth/me");
    return data;
  },
};
