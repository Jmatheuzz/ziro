export type SegmentoNegocio = "COMERCIO" | "SERVICOS" | "ALIMENTACAO" | "OUTRO";

export type TipoModulo = "FINANCEIRO" | "ESTOQUE" | "CLIENTES" | "VENDAS";

export interface CriarEmpresaPayload {
  nomeFantasia: string;
  razaoSocial?: string;
  cnpjCpf?: string;
  segmento: SegmentoNegocio;
}

export interface EmpresaResponse {
  id: string;
  nomeFantasia: string;
  razaoSocial: string | null;
  cnpjCpf: string | null;
  segmento: SegmentoNegocio;
  ativa: boolean;
}

export interface ModuloConfiguracaoResponse {
  id: string;
  modulo: TipoModulo;
  ativo: boolean;
  configuracaoJson: string | null;
}

export interface AtualizarModuloPayload {
  ativo?: boolean;
  configuracaoJson?: string;
}
