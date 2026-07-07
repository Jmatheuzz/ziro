import type { ButtonHTMLAttributes, ReactNode } from "react";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "ghost";
  carregando?: boolean;
  children: ReactNode;
}

export function Button({
  variant = "primary",
  carregando = false,
  disabled,
  children,
  className = "",
  ...props
}: ButtonProps) {
  const base =
    "w-full rounded-lg px-4 py-2.5 text-sm font-medium transition-standard focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-60";

  const variantes = {
    primary: "bg-ink text-paper hover:bg-ink-600 active:bg-ink-900",
    ghost: "bg-transparent text-ink hover:bg-ink-50",
  };

  return (
    <button
      className={`${base} ${variantes[variant]} ${className}`}
      disabled={disabled || carregando}
      {...props}
    >
      {carregando ? (
        <span className="flex items-center justify-center gap-2">
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
          Aguenta aí...
        </span>
      ) : (
        children
      )}
    </button>
  );
}
