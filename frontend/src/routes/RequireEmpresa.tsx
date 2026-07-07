import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { ModulosProvider } from "@/context/ModulosContext";

/**
 * So deixa passar quem ja tem empresa cadastrada. Quem ainda nao tem
 * cai automaticamente no onboarding - o usuario nunca fica "perdido"
 * numa tela que nao faz sentido pro estado dele.
 */
export function RequireEmpresa() {
  const { usuario } = useAuth();

  if (!usuario?.empresaId) {
    return <Navigate to="/onboarding" replace />;
  }

  return (
    <ModulosProvider>
      <Outlet />
    </ModulosProvider>
  );
}
