import { api } from "./client";
import type { ClientePayload, ClienteResponse, PaginaResponse } from "@/types/cliente";

interface ListarParams {
  busca?: string;
  page?: number;
  size?: number;
}

export const clienteApi = {
  async listar(params: ListarParams): Promise<PaginaResponse<ClienteResponse>> {
    const { data } = await api.get<PaginaResponse<ClienteResponse>>("/api/clientes", { params });
    return data;
  },

  async buscar(id: string): Promise<ClienteResponse> {
    const { data } = await api.get<ClienteResponse>(`/api/clientes/${id}`);
    return data;
  },

  async criar(payload: ClientePayload): Promise<ClienteResponse> {
    const { data } = await api.post<ClienteResponse>("/api/clientes", payload);
    return data;
  },

  async atualizar(id: string, payload: ClientePayload): Promise<ClienteResponse> {
    const { data } = await api.put<ClienteResponse>(`/api/clientes/${id}`, payload);
    return data;
  },

  async excluir(id: string): Promise<void> {
    await api.delete(`/api/clientes/${id}`);
  },
};
