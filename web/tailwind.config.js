/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        /* 60-30-10 light palette */
        surface: {
          DEFAULT: "#FFFFFF",
          muted: "#F9FAFB",
        },
        ink: {
          DEFAULT: "#111827",
          secondary: "#4B5563",
          muted: "#6B7280",
        },
        accent: {
          DEFAULT: "#10B981",
          hover: "#059669",
          light: "#D1FAE5",
          muted: "#ECFDF5",
        },
        danger: {
          DEFAULT: "#EF4444",
          hover: "#DC2626",
          light: "#FEF2F2",
          border: "#FECACA",
        },
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
      },
      fontSize: {
        base: ["14px", { lineHeight: "1.5" }],
        lg: ["16px", { lineHeight: "1.5" }],
      },
      boxShadow: {
        card: "0 1px 3px 0 rgb(17 24 39 / 0.06), 0 1px 2px -1px rgb(17 24 39 / 0.06)",
        cardHover: "0 4px 12px 0 rgb(17 24 39 / 0.08)",
        focus: "0 0 0 3px rgb(16 185 129 / 0.25)",
      },
    },
  },
  plugins: [],
};
