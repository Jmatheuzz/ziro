import { api } from "./client";
import type { AtualizarPerfilPayload, TrocarSenhaPayload } from "@/types/usuario";
import type { MensagemResponse } from "@/types/auth";

export const usuarioApi = {
  async atualizarPerfil(payload: AtualizarPerfilPayload): Promise<MensagemResponse> {
    const { data } = await api.put<MensagemResponse>("/api/usuarios/me", payload);
    return data;
  },
  async trocarSenha(payload: TrocarSenhaPayload): Promise<MensagemResponse> {
    const { data } = await api.post<MensagemResponse>("/api/usuarios/me/senha", payload);
    return data;
  },
  async modulosVisiveis(): Promise<string[]> {
    const { data } = await api.get<string[]>("/api/usuarios/me/modulos");
    return data;
  },
};
