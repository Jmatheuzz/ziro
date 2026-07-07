import { api } from "./client";
import type { RegistroAuditoriaResponse } from "@/types/auditoria";
import type { PaginaResponse } from "@/types/cliente";

interface ListarAuditoriaParams {
  entidade?: string;
  page?: number;
  size?: number;
}

export const auditoriaApi = {
  async listar(params: ListarAuditoriaParams): Promise<PaginaResponse<RegistroAuditoriaResponse>> {
    const { data } = await api.get<PaginaResponse<RegistroAuditoriaResponse>>("/api/auditoria", { params });
    return data;
  },
};
