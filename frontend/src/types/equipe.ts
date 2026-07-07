import type { TipoModulo } from "@/types/empresa";

export type StatusUsuario = "PENDENTE_VERIFICACAO" | "ATIVO" | "INATIVO";

export interface ConvidarOperadorPayload {
  nome: string;
  email: string;
  modulos: TipoModulo[];
}

export interface AtualizarPermissoesPayload {
  modulos: TipoModulo[];
}

export interface OperadorResponse {
  id: string;
  nome: string;
  email: string;
  status: StatusUsuario;
  modulos: TipoModulo[];
}
