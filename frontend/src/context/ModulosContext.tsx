import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { usuarioApi } from "@/api/usuario";
import type { TipoModulo } from "@/types/empresa";

interface ModulosContextValor {
  modulosVisiveis: TipoModulo[];
  carregando: boolean;
  moduloAtivo: (modulo: TipoModulo) => boolean;
  recarregarModulos: () => Promise<void>;
}

const ModulosContext = createContext<ModulosContextValor | undefined>(undefined);

export function ModulosProvider({ children }: { children: ReactNode }) {
  const [modulosVisiveis, setModulosVisiveis] = useState<TipoModulo[]>([]);
  const [carregando, setCarregando] = useState(true);

  async function carregar() {
    try {
      const lista = await usuarioApi.modulosVisiveis();
      setModulosVisiveis(lista as TipoModulo[]);
    } catch {
      setModulosVisiveis([]);
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  function moduloAtivo(modulo: TipoModulo): boolean {
    return modulosVisiveis.includes(modulo);
  }

  return (
    <ModulosContext.Provider value={{ modulosVisiveis, carregando, moduloAtivo, recarregarModulos: carregar }}>
      {children}
    </ModulosContext.Provider>
  );
}

export function useModulos(): ModulosContextValor {
  const contexto = useContext(ModulosContext);
  if (!contexto) {
    throw new Error("useModulos precisa ser usado dentro de um ModulosProvider");
  }
  return contexto;
}
