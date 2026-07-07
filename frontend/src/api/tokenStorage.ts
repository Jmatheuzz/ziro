const CHAVE_ACCESS_TOKEN = "ziro_access_token";
const CHAVE_REFRESH_TOKEN = "ziro_refresh_token";

/**
 * MVP: tokens ficam em localStorage pra simplificar.
 * Pra producao mais adiante, vale considerar mover o refresh token
 * pra um cookie httpOnly emitido pelo backend, o que reduz exposicao a XSS.
 */
export const tokenStorage = {
  getAccessToken(): string | null {
    return localStorage.getItem(CHAVE_ACCESS_TOKEN);
  },
  getRefreshToken(): string | null {
    return localStorage.getItem(CHAVE_REFRESH_TOKEN);
  },
  salvar(accessToken: string, refreshToken: string) {
    localStorage.setItem(CHAVE_ACCESS_TOKEN, accessToken);
    localStorage.setItem(CHAVE_REFRESH_TOKEN, refreshToken);
  },
  limpar() {
    localStorage.removeItem(CHAVE_ACCESS_TOKEN);
    localStorage.removeItem(CHAVE_REFRESH_TOKEN);
  },
};
