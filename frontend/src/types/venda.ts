export type StatusVenda = "ATIVA" | "CANCELADA";

export type FormaPagamento = "DINHEIRO" | "CARTAO" | "PIX" | "FIADO";

export interface ItemVendaPayload {
  produtoId: string;
  quantidade: number;
}

export interface ItemVendaResponse {
  produtoId: string | null;
  nomeProduto: string;
  quantidade: number;
  precoUnitario: number;
  subtotal: number;
}

export interface CriarVendaPayload {
  clienteId?: string;
  dataVenda?: string;
  formaPagamento: FormaPagamento;
  desconto?: number;
  observacoes?: string;
  itens: ItemVendaPayload[];
}

export interface VendaResponse {
  id: string;
  clienteId: string | null;
  clienteNome: string | null;
  dataVenda: string;
  status: StatusVenda;
  formaPagamento: FormaPagamento;
  valorTotal: number;
  desconto: number;
  observacoes: string | null;
  itens: ItemVendaResponse[];
}

export interface VendasResumoResponse {
  totalVendidoMesAtual: number;
  quantidadeVendasMesAtual: number;
  ticketMedioMesAtual: number;
}
