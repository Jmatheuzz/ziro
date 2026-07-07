import { isAxiosError } from "axios";
import type { ErroApi } from "@/types/auth";

export function extrairMensagemErro(erro: unknown, mensagemPadrao = "Algo deu errado. Tenta de novo."): string {
  if (isAxiosError<ErroApi>(erro)) {
    return erro.response?.data?.mensagem ?? mensagemPadrao;
  }
  return mensagemPadrao;
}
