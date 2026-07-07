import { Link } from "react-router-dom";
import type { ReactNode } from "react";

interface BackLinkProps {
  to: string;
  children: ReactNode;
  variant?: "default" | "auth";
  className?: string;
}

export function BackLink({ to, children, variant = "default", className = "" }: BackLinkProps) {
  const colors =
    variant === "auth"
      ? "text-ink hover:text-brass-700"
      : "text-ink-400 hover:text-ink";

  return (
    <Link
      to={to}
      className={`group inline-flex items-center gap-1.5 text-sm font-medium transition-standard ${colors} ${className}`}
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="15"
        height="15"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        className="transition-standard group-hover:-translate-x-1"
        aria-hidden="true"
      >
        <path d="m15 18-6-6 6-6" />
      </svg>
      {children}
    </Link>
  );
}
