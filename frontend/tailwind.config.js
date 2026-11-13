/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#f0f7ff',
          100: '#e0effe',
          200: '#bae0fd',
          300: '#7cc4fa',
          400: '#38a5f6',
          500: '#0ea5e9', // Vibrant Light Blue / Sky
          600: '#0284c7', // Royal Blue
          700: '#0369a1', // Deep Blue
          800: '#075985',
          900: '#0c4a6e',
          950: '#082f49',
        },
        navy: {
          800: '#172554',
          900: '#0f172a',
          950: '#020617',
        },
        accent: {
          blue: '#1e40af',
          sky: '#38bdf8',
          ice: '#f0f9ff',
        }
      },
      fontFamily: {
        sans: ['Poppins', 'system-ui', '-apple-system', 'sans-serif'],
        poppins: ['Poppins', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      boxShadow: {
        'soft': '0 2px 15px -3px rgba(14, 165, 233, 0.06), 0 4px 6px -2px rgba(0, 0, 0, 0.02)',
        'card': '0 4px 20px -2px rgba(15, 23, 42, 0.05)',
        'elevated': '0 10px 25px -5px rgba(2, 132, 199, 0.1), 0 8px 10px -6px rgba(15, 23, 42, 0.04)',
      }
    },
  },
  plugins: [],
}
