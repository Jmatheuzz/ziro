interface SwitchProps {
  ativo: boolean;
  onChange: (novoValor: boolean) => void;
  disabled?: boolean;
  rotulo: string;
}

export function Switch({ ativo, onChange, disabled, rotulo }: SwitchProps) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={ativo}
      aria-label={rotulo}
      disabled={disabled}
      onClick={() => onChange(!ativo)}
      className={`relative h-6 w-11 shrink-0 rounded-full transition-standard disabled:cursor-not-allowed disabled:opacity-60
        ${ativo ? "bg-ink" : "bg-ink-100"}`}
    >
      <span
        className={`absolute top-0.5 h-5 w-5 rounded-full bg-white shadow-sm transition-standard
          ${ativo ? "left-[22px]" : "left-0.5"}`}
      />
    </button>
  );
}
