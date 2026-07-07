/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        // Tinta - azul quase-preto, cor de marca principal
        ink: {
          DEFAULT: "#12213B",
          50: "#EEF1F6",
          100: "#D7DDE9",
          400: "#4A5C82",
          600: "#243A5E",
          900: "#0B1524",
        },
        // Papel - fundo claro, levemente acinzentado (nao e o cream generico)
        paper: {
          DEFAULT: "#F7F7F4",
          dim: "#EFEEE9",
        },
        // Latao - acento dourado queimado, usado com moderacao pra acao/destaque
        brass: {
          DEFAULT: "#E3A23C",
          600: "#C9832240",
          700: "#B87A26",
        },
        // Estados
        sage: "#5C8A6E",
        rust: "#B0503F",
      },
      fontFamily: {
        display: ["'Space Grotesk'", "sans-serif"],
        sans: ["Inter", "sans-serif"],
        mono: ["'JetBrains Mono'", "monospace"],
      },
      borderRadius: {
        xl2: "1.25rem",
      },
      boxShadow: {
        card: "0 1px 2px rgba(18,33,59,0.04), 0 8px 24px -12px rgba(18,33,59,0.15)",
      },
    },
  },
  plugins: [],
};
