import { api } from "./client";
import type {
  AjustarEstoquePayload,
  CategoriaPayload,
  CategoriaResponse,
  EstoqueResumoResponse,
  ProdutoPayload,
  ProdutoResponse,
} from "@/types/estoque";
import type { PaginaResponse } from "@/types/cliente";

interface ListarProdutosParams {
  busca?: string;
  categoriaId?: string;
  page?: number;
  size?: number;
}

export const produtoApi = {
  async listar(params: ListarProdutosParams): Promise<PaginaResponse<ProdutoResponse>> {
    const { data } = await api.get<PaginaResponse<ProdutoResponse>>("/api/produtos", { params });
    return data;
  },
  async buscar(id: string): Promise<ProdutoResponse> {
    const { data } = await api.get<ProdutoResponse>(`/api/produtos/${id}`);
    return data;
  },
  async criar(payload: ProdutoPayload): Promise<ProdutoResponse> {
    const { data } = await api.post<ProdutoResponse>("/api/produtos", payload);
    return data;
  },
  async atualizar(id: string, payload: Omit<ProdutoPayload, "quantidadeEstoque">): Promise<ProdutoResponse> {
    const { data } = await api.put<ProdutoResponse>(`/api/produtos/${id}`, payload);
    return data;
  },
  async ajustarEstoque(id: string, payload: AjustarEstoquePayload): Promise<ProdutoResponse> {
    const { data } = await api.patch<ProdutoResponse>(`/api/produtos/${id}/estoque`, payload);
    return data;
  },
  async excluir(id: string): Promise<void> {
    await api.delete(`/api/produtos/${id}`);
  },
};

export const categoriaApi = {
  async listar(): Promise<CategoriaResponse[]> {
    const { data } = await api.get<CategoriaResponse[]>("/api/categorias");
    return data;
  },
  async criar(payload: CategoriaPayload): Promise<CategoriaResponse> {
    const { data } = await api.post<CategoriaResponse>("/api/categorias", payload);
    return data;
  },
  async excluir(id: string): Promise<void> {
    await api.delete(`/api/categorias/${id}`);
  },
};

export const estoqueResumoApi = {
  async buscar(): Promise<EstoqueResumoResponse> {
    const { data } = await api.get<EstoqueResumoResponse>("/api/estoque/resumo");
    return data;
  },
};
