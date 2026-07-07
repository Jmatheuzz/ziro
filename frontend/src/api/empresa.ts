import { api } from "./client";
import type { CriarEmpresaPayload, EmpresaResponse } from "@/types/empresa";

export const empresaApi = {
  async criar(payload: CriarEmpresaPayload): Promise<EmpresaResponse> {
    const { data } = await api.post<EmpresaResponse>("/api/empresas", payload);
    return data;
  },

  async buscarMinhaEmpresa(): Promise<EmpresaResponse> {
    const { data } = await api.get<EmpresaResponse>("/api/empresas/me");
    return data;
  },

  async atualizar(payload: CriarEmpresaPayload): Promise<EmpresaResponse> {
    const { data } = await api.put<EmpresaResponse>("/api/empresas/me", payload);
    return data;
  },
};
