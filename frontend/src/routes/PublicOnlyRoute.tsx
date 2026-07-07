import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";

export function PublicOnlyRoute() {
  const { usuario, carregando } = useAuth();

  if (carregando) return null;

  if (usuario) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
