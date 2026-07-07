import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";

/**
 * Inverso do RequireEmpresa: usado na propria tela de onboarding,
 * pra quem ja tem empresa nao conseguir cadastrar uma segunda.
 */
export function RequireNoEmpresa() {
  const { usuario } = useAuth();

  if (usuario?.empresaId) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
