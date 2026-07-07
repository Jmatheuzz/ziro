import type { StatusConta } from "@/types/financeiro";

const ESTILOS: Record<StatusConta, string> = {
  ABERTA: "bg-ink-50 text-ink-600",
  PAGA: "bg-sage/10 text-sage",
  RECEBIDA: "bg-sage/10 text-sage",
  VENCIDA: "bg-rust/10 text-rust",
  CANCELADA: "bg-ink-50 text-ink-400",
};

const ROTULOS: Record<StatusConta, string> = {
  ABERTA: "Em aberto",
  PAGA: "Paga",
  RECEBIDA: "Recebida",
  VENCIDA: "Vencida",
  CANCELADA: "Cancelada",
};

export function StatusContaBadge({ status, atrasada }: { status: StatusConta; atrasada: boolean }) {
  if (atrasada) {
    return <span className="rounded-full bg-rust/10 px-2 py-0.5 text-xs font-medium text-rust">Vencida</span>;
  }
  return <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${ESTILOS[status]}`}>{ROTULOS[status]}</span>;
}
