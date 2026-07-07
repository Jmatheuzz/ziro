export interface RegistroAuditoriaResponse {
  id: string;
  usuarioNome: string | null;
  entidade: string;
  entidadeId: string | null;
  acao: string;
  descricao: string;
  criadoEm: string; // ISO datetime
}
