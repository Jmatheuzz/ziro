import { api } from "./client";
import type { AtualizarModuloPayload, ModuloConfiguracaoResponse, TipoModulo } from "@/types/empresa";

export const moduloApi = {
  async listar(): Promise<ModuloConfiguracaoResponse[]> {
    const { data } = await api.get<ModuloConfiguracaoResponse[]>("/api/modulos");
    return data;
  },

  async atualizar(modulo: TipoModulo, payload: AtualizarModuloPayload): Promise<ModuloConfiguracaoResponse> {
    const { data } = await api.patch<ModuloConfiguracaoResponse>(`/api/modulos/${modulo}`, payload);
    return data;
  },
};
