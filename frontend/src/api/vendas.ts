import { api } from "./client";
import type { CriarVendaPayload, StatusVenda, VendaResponse, VendasResumoResponse } from "@/types/venda";
import type { PaginaResponse } from "@/types/cliente";

interface ListarVendasParams {
  status?: StatusVenda;
  page?: number;
  size?: number;
}

export const vendaApi = {
  async listar(params: ListarVendasParams): Promise<PaginaResponse<VendaResponse>> {
    const { data } = await api.get<PaginaResponse<VendaResponse>>("/api/vendas", { params });
    return data;
  },
  async buscar(id: string): Promise<VendaResponse> {
    const { data } = await api.get<VendaResponse>(`/api/vendas/${id}`);
    return data;
  },
  async criar(payload: CriarVendaPayload): Promise<VendaResponse> {
    const { data } = await api.post<VendaResponse>("/api/vendas", payload);
    return data;
  },
  async cancelar(id: string): Promise<void> {
    await api.delete(`/api/vendas/${id}`);
  },
  async resumo(): Promise<VendasResumoResponse> {
    const { data } = await api.get<VendasResumoResponse>("/api/vendas/resumo");
    return data;
  },
};
