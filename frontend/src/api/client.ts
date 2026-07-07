import axios, { type AxiosError, type InternalAxiosRequestConfig } from "axios";
import { tokenStorage } from "./tokenStorage";
import type { TokenResponse } from "@/types/auth";

const baseURL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export const api = axios.create({ baseURL });

// instancia "limpa", sem interceptors, usada so pra chamar /refresh
// (evita loop infinito se o proprio refresh devolver 401)
const apiSemInterceptor = axios.create({ baseURL });

api.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshEmAndamento: Promise<string | null> | null = null;

async function renovarToken(): Promise<string | null> {
  const refreshToken = tokenStorage.getRefreshToken();
  if (!refreshToken) return null;

  try {
    const { data } = await apiSemInterceptor.post<TokenResponse>("/api/auth/refresh", {
      refreshToken,
    });
    tokenStorage.salvar(data.accessToken, data.refreshToken);
    return data.accessToken;
  } catch {
    tokenStorage.limpar();
    return null;
  }
}

interface RequisicaoComRetry extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const requisicaoOriginal = error.config as RequisicaoComRetry | undefined;

    const rotaDeAuth = requisicaoOriginal?.url?.startsWith("/api/auth/");
    if (error.response?.status !== 401 || !requisicaoOriginal || requisicaoOriginal._retry || rotaDeAuth) {
      return Promise.reject(error);
    }

    requisicaoOriginal._retry = true;

    // se ja tem um refresh rolando, todo mundo espera o mesmo resultado
    refreshEmAndamento = refreshEmAndamento ?? renovarToken().finally(() => {
      refreshEmAndamento = null;
    });

    const novoToken = await refreshEmAndamento;

    if (!novoToken) {
      window.location.href = "/login";
      return Promise.reject(error);
    }

    requisicaoOriginal.headers.Authorization = `Bearer ${novoToken}`;
    return api(requisicaoOriginal);
  }
);
