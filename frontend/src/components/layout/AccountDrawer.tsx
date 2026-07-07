import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";

interface AccountDrawerProps {
  aberto: boolean;
  onFechar: () => void;
}

const ITENS_ADMIN = [
  { rota: "/configuracoes/personalizacao", rotulo: "Personalização", descricao: "Ligue ou desligue módulos" },
  { rota: "/configuracoes/equipe", rotulo: "Equipe", descricao: "Convide e gerencie quem tem acesso" },
  { rota: "/configuracoes/auditoria", rotulo: "Histórico de ações", descricao: "O que mudou e quem mudou" },
];

const ITENS_TODOS = [
  { rota: "/configuracoes/conta", rotulo: "Gerenciar conta", descricao: "Seu nome e sua senha" },
];

export function AccountDrawer({ aberto, onFechar }: AccountDrawerProps) {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();

  const itens = usuario?.role === "ADMIN" ? [...ITENS_ADMIN, ...ITENS_TODOS] : ITENS_TODOS;

  async function handleLogout() {
    onFechar();
    await logout();
    navigate("/login", { replace: true });
  }

  return (
    <>
      {/* overlay */}
      <div
        onClick={onFechar}
        className={`fixed inset-0 z-40 bg-ink/20 transition-standard ${
          aberto ? "opacity-100" : "pointer-events-none opacity-0"
        }`}
      />

      {/* painel */}
      <aside
        className={`fixed right-0 top-0 z-50 h-full w-80 max-w-[85vw] bg-white shadow-xl transition-transform duration-200 ease-out ${
          aberto ? "translate-x-0" : "translate-x-full"
        }`}
      >
        <div className="flex h-full flex-col">
          <div className="border-b border-ink-100 px-6 py-5">
            <p className="font-display text-base font-semibold text-ink">{usuario?.nome}</p>
            <p className="mt-0.5 text-xs text-ink-400">{usuario?.email}</p>
            {usuario?.role === "OPERADOR" && (
              <span className="mt-2 inline-block rounded-full bg-ink-50 px-2 py-0.5 text-xs font-medium text-ink-400">
                Operador
              </span>
            )}
          </div>

          <nav className="flex-1 overflow-y-auto px-3 py-3">
            {itens.map((item) => (
              <Link
                key={item.rota}
                to={item.rota}
                onClick={onFechar}
                className="block rounded-lg px-3 py-2.5 transition-standard hover:bg-ink-50"
              >
                <span className="block text-sm font-medium text-ink">{item.rotulo}</span>
                <span className="block text-xs text-ink-400">{item.descricao}</span>
              </Link>
            ))}
          </nav>

          <div className="border-t border-ink-100 px-3 py-3">
            <button
              onClick={handleLogout}
              className="w-full rounded-lg px-3 py-2.5 text-left text-sm font-medium text-rust transition-standard hover:bg-rust/10"
            >
              Sair
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}
