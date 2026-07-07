import type { ReactNode } from "react";
import { ZiroMark } from "./ZiroMark";

interface AuthLayoutProps {
  titulo: string;
  subtitulo?: string;
  children: ReactNode;
}

export function AuthLayout({ titulo, subtitulo, children }: AuthLayoutProps) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      {/* Painel de marca - some no mobile pra nao roubar espaco do formulario */}
      <div className="relative hidden flex-col justify-between overflow-hidden bg-ink px-12 py-12 text-paper lg:flex">
        <div className="font-display text-lg font-semibold tracking-tight">Ziro</div>

        <div className="flex flex-col gap-8">
          <ZiroMark size={72} className="text-brass" />
          <div className="max-w-sm">
            <h2 className="font-display text-3xl font-semibold leading-tight">
              Gestão que começa do zero e cresce com o seu negócio.
            </h2>
            <p className="mt-4 text-sm leading-relaxed text-ink-100">
              Sem módulo que você não usa, sem tela que você não entende.
              Só o que o seu negócio precisa, do seu jeito.
            </p>
          </div>
        </div>

        <p className="font-mono text-xs text-ink-100/70">© {new Date().getFullYear()} Ziro</p>
      </div>

      {/* Painel do formulario */}
      <div className="flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-sm">
          <div className="mb-8 lg:hidden">
            <ZiroMark size={40} className="text-ink" />
          </div>
          <h1 className="font-display text-2xl font-semibold text-ink">{titulo}</h1>
          {subtitulo && <p className="mt-2 text-sm text-ink-400">{subtitulo}</p>}
          <div className="mt-8">{children}</div>
        </div>
      </div>
    </div>
  );
}
