export interface ClientePayload {
  nome: string;
  telefone?: string;
  email?: string;
  cpfCnpj?: string;
  observacoes?: string;
}

export interface ClienteResponse {
  id: string;
  nome: string;
  telefone: string | null;
  email: string | null;
  cpfCnpj: string | null;
  observacoes: string | null;
  ativo: boolean;
}

export interface PaginaResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // pagina atual, 0-indexed
  size: number;
  first: boolean;
  last: boolean;
}
