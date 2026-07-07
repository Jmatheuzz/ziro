import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "@/context/AuthContext";
import { ProtectedRoute } from "@/routes/ProtectedRoute";
import { PublicOnlyRoute } from "@/routes/PublicOnlyRoute";
import { RequireEmpresa } from "@/routes/RequireEmpresa";
import { RequireNoEmpresa } from "@/routes/RequireNoEmpresa";
import { LoginPage } from "@/pages/auth/LoginPage";
import { RegisterPage } from "@/pages/auth/RegisterPage";
import { VerifyEmailPage } from "@/pages/auth/VerifyEmailPage";
import { ForgotPasswordPage } from "@/pages/auth/ForgotPasswordPage";
import { ResetPasswordPage } from "@/pages/auth/ResetPasswordPage";
import { OnboardingPage } from "@/pages/onboarding/OnboardingPage";
import { PersonalizacaoPage } from "@/pages/configuracoes/PersonalizacaoPage";
import { ContaPage } from "@/pages/configuracoes/conta/ContaPage";
import { AuditoriaPage } from "@/pages/configuracoes/auditoria/AuditoriaPage";
import { EquipePage } from "@/pages/configuracoes/equipe/EquipePage";
import { ClientesListPage } from "@/pages/clientes/ClientesListPage";
import { ClienteFormPage } from "@/pages/clientes/ClienteFormPage";
import { FinanceiroOverviewPage } from "@/pages/financeiro/FinanceiroOverviewPage";
import { ContasPagarPage } from "@/pages/financeiro/ContasPagarPage";
import { ContaPagarFormPage } from "@/pages/financeiro/ContaPagarFormPage";
import { ContasReceberPage } from "@/pages/financeiro/ContasReceberPage";
import { ContaReceberFormPage } from "@/pages/financeiro/ContaReceberFormPage";
import { FluxoCaixaPage } from "@/pages/financeiro/FluxoCaixaPage";
import { EstoqueOverviewPage } from "@/pages/estoque/EstoqueOverviewPage";
import { ProdutosListPage } from "@/pages/estoque/ProdutosListPage";
import { ProdutoFormPage } from "@/pages/estoque/ProdutoFormPage";
import { AjustarEstoquePage } from "@/pages/estoque/AjustarEstoquePage";
import { VendasOverviewPage } from "@/pages/vendas/VendasOverviewPage";
import { VendasListPage } from "@/pages/vendas/VendasListPage";
import { VendaFormPage } from "@/pages/vendas/VendaFormPage";
import { VendaDetailPage } from "@/pages/vendas/VendaDetailPage";
import { DashboardPage } from "@/pages/DashboardPage";

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<PublicOnlyRoute />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/registrar" element={<RegisterPage />} />
            <Route path="/esqueci-senha" element={<ForgotPasswordPage />} />
            <Route path="/redefinir-senha" element={<ResetPasswordPage />} />
          </Route>

          {/* verificacao de email fica acessivel independente de estado de login */}
          <Route path="/verificar-email" element={<VerifyEmailPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<RequireNoEmpresa />}>
              <Route path="/onboarding" element={<OnboardingPage />} />
            </Route>

            <Route element={<RequireEmpresa />}>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/configuracoes/personalizacao" element={<PersonalizacaoPage />} />
              <Route path="/configuracoes/conta" element={<ContaPage />} />
              <Route path="/configuracoes/auditoria" element={<AuditoriaPage />} />
              <Route path="/configuracoes/equipe" element={<EquipePage />} />

              <Route path="/clientes" element={<ClientesListPage />} />
              <Route path="/clientes/novo" element={<ClienteFormPage />} />
              <Route path="/clientes/:id/editar" element={<ClienteFormPage />} />

              <Route path="/financeiro" element={<FinanceiroOverviewPage />} />
              <Route path="/financeiro/contas-a-pagar" element={<ContasPagarPage />} />
              <Route path="/financeiro/contas-a-pagar/nova" element={<ContaPagarFormPage />} />
              <Route path="/financeiro/contas-a-pagar/:id/editar" element={<ContaPagarFormPage />} />
              <Route path="/financeiro/contas-a-receber" element={<ContasReceberPage />} />
              <Route path="/financeiro/contas-a-receber/nova" element={<ContaReceberFormPage />} />
              <Route path="/financeiro/contas-a-receber/:id/editar" element={<ContaReceberFormPage />} />
              <Route path="/financeiro/fluxo-caixa" element={<FluxoCaixaPage />} />

              <Route path="/estoque" element={<EstoqueOverviewPage />} />
              <Route path="/estoque/produtos" element={<ProdutosListPage />} />
              <Route path="/estoque/produtos/novo" element={<ProdutoFormPage />} />
              <Route path="/estoque/produtos/:id/editar" element={<ProdutoFormPage />} />
              <Route path="/estoque/produtos/:id/ajustar" element={<AjustarEstoquePage />} />

              <Route path="/vendas" element={<VendasOverviewPage />} />
              <Route path="/vendas/historico" element={<VendasListPage />} />
              <Route path="/vendas/nova" element={<VendaFormPage />} />
              <Route path="/vendas/:id" element={<VendaDetailPage />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
