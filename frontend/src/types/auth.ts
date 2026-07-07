export interface RegisterPayload {
  nome: string;
  email: string;
  senha: string;
}

export interface LoginPayload {
  email: string;
  senha: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiraEmSegundos: number;
}

export interface MensagemResponse {
  mensagem: string;
}

export interface UsuarioAutenticado {
  id: string;
  nome: string;
  email: string;
  role: "ADMIN" | "OPERADOR";
  empresaId: string | null;
}

export interface ErroApi {
  timestamp: string;
  status: number;
  mensagem: string;
  campos?: Record<string, string>;
}
