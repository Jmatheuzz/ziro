import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { useModulos } from "@/context/ModulosContext";
import { ZiroMark } from "@/components/auth/ZiroMark";
import { AccountDrawer } from "@/components/layout/AccountDrawer";
import type { TipoModulo } from "@/types/empresa";

const LINKS: { rota: string; rotulo: string; modulo?: TipoModulo }[] = [
  { rota: "/", rotulo: "Início" },
  { rota: "/clientes", rotulo: "Clientes", modulo: "CLIENTES" },
  { rota: "/estoque", rotulo: "Estoque", modulo: "ESTOQUE" },
  { rota: "/vendas", rotulo: "Vendas", modulo: "VENDAS" },
  { rota: "/financeiro", rotulo: "Financeiro", modulo: "FINANCEIRO" },
];

export function AppHeader() {
  const { usuario } = useAuth();
  const { moduloAtivo } = useModulos();
  const location = useLocation();
  const [drawerAberto, setDrawerAberto] = useState(false);

  const linksVisiveis = LINKS.filter((link) => !link.modulo || moduloAtivo(link.modulo));

  return (
    <>
      <header className="border-b border-ink-100 px-6 py-4">
        <div className="mx-auto flex max-w-5xl items-center justify-between">
          <div className="flex items-center gap-8">
            <div className="flex items-center gap-2">
              <ZiroMark size={26} className="text-ink" />
              <span className="font-display text-lg font-semibold text-ink">Ziro</span>
            </div>

            {usuario?.empresaId && (
              <nav className="hidden gap-1 sm:flex">
                {linksVisiveis.map((link) => {
                  const ativo =
                    link.rota === "/" ? location.pathname === "/" : location.pathname.startsWith(link.rota);
                  return (
                    <Link
                      key={link.rota}
                      to={link.rota}
                      className={`rounded-md px-3 py-1.5 text-sm font-medium transition-standard ${
                        ativo ? "bg-ink-50 text-ink" : "text-ink-400 hover:text-ink"
                      }`}
                    >
                      {link.rotulo}
                    </Link>
                  );
                })}
              </nav>
            )}
          </div>

          <button
            onClick={() => setDrawerAberto(true)}
            aria-label="Abrir menu da conta"
            className="flex h-9 w-9 items-center justify-center rounded-full bg-ink-50 text-sm font-semibold text-ink transition-standard hover:bg-ink-100"
          >
            {usuario?.nome?.charAt(0).toUpperCase() ?? "?"}
          </button>
        </div>
      </header>

      <AccountDrawer aberto={drawerAberto} onFechar={() => setDrawerAberto(false)} />
    </>
  );
}
