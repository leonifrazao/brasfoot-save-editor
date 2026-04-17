import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: "class",
  content: [
    "./src/app/**/*.{ts,tsx}",
    "./src/components/**/*.{ts,tsx}",
    "./src/lib/**/*.{ts,tsx}",
  ],
  theme: {
    extend: {
      screens: {
        tablet: "768px",
        desktop: "1024px",
        wide: "1440px",
      },
      maxWidth: {
        shell: "1440px",
        content: "1280px",
      },
      boxShadow: {
        shell: "0 30px 80px -42px rgba(0, 0, 0, 0.72)",
        panel: "0 18px 40px -24px rgba(0, 0, 0, 0.56)",
        glow: "0 0 0 1px rgba(23, 104, 255, 0.16), 0 18px 44px -26px rgba(23, 104, 255, 0.34)",
      },
      borderRadius: {
        "4xl": "2rem",
      },
      backgroundImage: {
        "hero-grid":
          "linear-gradient(rgba(255,255,255,0.028) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.028) 1px, transparent 1px)",
      },
      transitionTimingFunction: {
        productive: "cubic-bezier(0.16, 1, 0.3, 1)",
      },
    },
  },
  plugins: [],
};

export default config;
