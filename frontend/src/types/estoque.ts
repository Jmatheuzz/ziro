export type TipoMovimentacaoEstoque = "ENTRADA" | "SAIDA";

export interface ProdutoPayload {
  nome: string;
  descricao?: string;
  precoVenda: number;
  precoCusto?: number;
  quantidadeEstoque?: number;
  estoqueMinimo?: number;
  sku?: string;
  categoriaId?: string;
}

export interface ProdutoResponse {
  id: string;
  nome: string;
  descricao: string | null;
  precoVenda: number;
  precoCusto: number | null;
  quantidadeEstoque: number | null;
  estoqueMinimo: number | null;
  sku: string | null;
  categoriaId: string | null;
  categoriaNome: string | null;
  ativo: boolean;
  estoqueBaixo: boolean;
}

export interface AjustarEstoquePayload {
  tipo: TipoMovimentacaoEstoque;
  quantidade: number;
  motivo?: string;
}

export interface CategoriaPayload {
  nome: string;
}

export interface CategoriaResponse {
  id: string;
  nome: string;
}

export interface EstoqueResumoResponse {
  totalProdutos: number;
  produtosComEstoqueBaixo: number;
  valorTotalEstoque: number;
}
