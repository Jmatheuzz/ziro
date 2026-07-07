export type StatusConta = "ABERTA" | "PAGA" | "RECEBIDA" | "VENCIDA" | "CANCELADA";

export type TipoMovimentacao = "ENTRADA" | "SAIDA";

export interface ContaPagarPayload {
  descricao: string;
  valor: number;
  dataVencimento: string; // ISO yyyy-MM-dd
  fornecedor?: string;
  categoria?: string;
}

export interface ContaPagarResponse {
  id: string;
  descricao: string;
  valor: number;
  dataVencimento: string;
  dataPagamento: string | null;
  fornecedor: string | null;
  categoria: string | null;
  status: StatusConta;
  atrasada: boolean;
}

export interface ContaReceberPayload {
  descricao: string;
  valor: number;
  dataVencimento: string;
  clienteId?: string;
}

export interface ContaReceberResponse {
  id: string;
  descricao: string;
  valor: number;
  dataVencimento: string;
  dataRecebimento: string | null;
  clienteId: string | null;
  clienteNome: string | null;
  status: StatusConta;
  atrasada: boolean;
}

export interface MovimentacaoCaixaPayload {
  tipo: TipoMovimentacao;
  valor: number;
  descricao: string;
  data: string;
}

export interface MovimentacaoCaixaResponse {
  id: string;
  tipo: TipoMovimentacao;
  valor: number;
  descricao: string;
  data: string;
  origem: string | null;
}

export interface FluxoCaixaResponse {
  movimentacoes: MovimentacaoCaixaResponse[];
  totalEntradas: number;
  totalSaidas: number;
  saldo: number;
}

export interface ResumoFinanceiroResponse {
  totalAPagarEmAberto: number;
  totalAReceberEmAberto: number;
  saldoCaixaMesAtual: number;
  contasPagarVencidas: number;
  contasReceberVencidas: number;
}
