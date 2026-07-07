import { api } from "./client";
import type { AtualizarPermissoesPayload, ConvidarOperadorPayload, OperadorResponse } from "@/types/equipe";

export const equipeApi = {
  async listar(): Promise<OperadorResponse[]> {
    const { data } = await api.get<OperadorResponse[]>("/api/equipe");
    return data;
  },
  async convidar(payload: ConvidarOperadorPayload): Promise<OperadorResponse> {
    const { data } = await api.post<OperadorResponse>("/api/equipe/convites", payload);
    return data;
  },
  async atualizarPermissoes(operadorId: string, payload: AtualizarPermissoesPayload): Promise<OperadorResponse> {
    const { data } = await api.put<OperadorResponse>(`/api/equipe/${operadorId}/permissoes`, payload);
    return data;
  },
  async desativar(operadorId: string): Promise<void> {
    await api.patch(`/api/equipe/${operadorId}/desativar`);
  },
};
