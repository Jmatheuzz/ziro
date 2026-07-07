import { forwardRef, type InputHTMLAttributes } from "react";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  erro?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, erro, id, className = "", ...props }, ref) => {
    const inputId = id ?? props.name;

    return (
      <div className="flex flex-col gap-1.5">
        <label htmlFor={inputId} className="text-sm font-medium text-ink-600">
          {label}
        </label>
        <input
          ref={ref}
          id={inputId}
          className={`rounded-lg border bg-white px-3.5 py-2.5 text-sm text-ink placeholder:text-ink-400/60
            transition-standard focus:outline-none focus:ring-2 focus:ring-brass/40
            ${erro ? "border-rust" : "border-ink-100"} ${className}`}
          aria-invalid={!!erro}
          aria-describedby={erro ? `${inputId}-erro` : undefined}
          {...props}
        />
        {erro && (
          <p id={`${inputId}-erro`} className="text-xs text-rust">
            {erro}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = "Input";
