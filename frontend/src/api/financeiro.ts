import { api } from "./client";
import type {
  ContaPagarPayload,
  ContaPagarResponse,
  ContaReceberPayload,
  ContaReceberResponse,
  FluxoCaixaResponse,
  MovimentacaoCaixaPayload,
  MovimentacaoCaixaResponse,
  ResumoFinanceiroResponse,
  StatusConta,
} from "@/types/financeiro";
import type { PaginaResponse } from "@/types/cliente";

interface ListarContasParams {
  status?: StatusConta;
  page?: number;
  size?: number;
}

export const contaPagarApi = {
  async listar(params: ListarContasParams): Promise<PaginaResponse<ContaPagarResponse>> {
    const { data } = await api.get<PaginaResponse<ContaPagarResponse>>("/api/financeiro/contas-pagar", { params });
    return data;
  },
  async buscar(id: string): Promise<ContaPagarResponse> {
    const { data } = await api.get<ContaPagarResponse>(`/api/financeiro/contas-pagar/${id}`);
    return data;
  },
  async criar(payload: ContaPagarPayload): Promise<ContaPagarResponse> {
    const { data } = await api.post<ContaPagarResponse>("/api/financeiro/contas-pagar", payload);
    return data;
  },
  async atualizar(id: string, payload: ContaPagarPayload): Promise<ContaPagarResponse> {
    const { data } = await api.put<ContaPagarResponse>(`/api/financeiro/contas-pagar/${id}`, payload);
    return data;
  },
  async marcarComoPaga(id: string, dataPagamento?: string): Promise<ContaPagarResponse> {
    const { data } = await api.patch<ContaPagarResponse>(`/api/financeiro/contas-pagar/${id}/pagar`, {
      dataPagamento: dataPagamento ?? null,
    });
    return data;
  },
  async cancelar(id: string): Promise<void> {
    await api.delete(`/api/financeiro/contas-pagar/${id}`);
  },
};

export const contaReceberApi = {
  async listar(params: ListarContasParams): Promise<PaginaResponse<ContaReceberResponse>> {
    const { data } = await api.get<PaginaResponse<ContaReceberResponse>>("/api/financeiro/contas-receber", { params });
    return data;
  },
  async buscar(id: string): Promise<ContaReceberResponse> {
    const { data } = await api.get<ContaReceberResponse>(`/api/financeiro/contas-receber/${id}`);
    return data;
  },
  async criar(payload: ContaReceberPayload): Promise<ContaReceberResponse> {
    const { data } = await api.post<ContaReceberResponse>("/api/financeiro/contas-receber", payload);
    return data;
  },
  async atualizar(id: string, payload: ContaReceberPayload): Promise<ContaReceberResponse> {
    const { data } = await api.put<ContaReceberResponse>(`/api/financeiro/contas-receber/${id}`, payload);
    return data;
  },
  async marcarComoRecebida(id: string, dataRecebimento?: string): Promise<ContaReceberResponse> {
    const { data } = await api.patch<ContaReceberResponse>(`/api/financeiro/contas-receber/${id}/receber`, {
      dataRecebimento: dataRecebimento ?? null,
    });
    return data;
  },
  async cancelar(id: string): Promise<void> {
    await api.delete(`/api/financeiro/contas-receber/${id}`);
  },
};

export const fluxoCaixaApi = {
  async listar(inicio?: string, fim?: string): Promise<FluxoCaixaResponse> {
    const { data } = await api.get<FluxoCaixaResponse>("/api/financeiro/fluxo-caixa", {
      params: { inicio, fim },
    });
    return data;
  },
  async lancarManual(payload: MovimentacaoCaixaPayload): Promise<MovimentacaoCaixaResponse> {
    const { data } = await api.post<MovimentacaoCaixaResponse>("/api/financeiro/fluxo-caixa", payload);
    return data;
  },
};

export const resumoFinanceiroApi = {
  async buscar(): Promise<ResumoFinanceiroResponse> {
    const { data } = await api.get<ResumoFinanceiroResponse>("/api/financeiro/resumo");
    return data;
  },
};
