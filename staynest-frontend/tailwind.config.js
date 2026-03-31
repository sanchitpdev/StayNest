/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary:   { DEFAULT: '#FF385C', hover: '#E31C5F', light: '#FFF1F2' },
        secondary: { DEFAULT: '#00A699', hover: '#008F84' },
        staynest:  { dark: '#222222', gray: '#717171', light: '#DDDDDD', bg: '#F7F7F7' },
      },
      fontFamily: {
        sans: ['"DM Sans"', 'sans-serif'],
      },
      boxShadow: {
        card:   '0 6px 16px rgba(0,0,0,0.12)',
        nav:    '0 1px 2px rgba(0,0,0,0.08), 0 4px 12px rgba(0,0,0,0.05)',
        widget: '0 2px 16px rgba(0,0,0,0.12), 0 6px 16px rgba(0,0,0,0.12)',
      },
      borderRadius: { xl2: '16px', xl3: '24px' },
      animation: { 'fade-in': 'fadeIn 0.2s ease-out' },
      keyframes:  { fadeIn: { '0%': { opacity: 0, transform: 'translateY(8px)' }, '100%': { opacity: 1, transform: 'translateY(0)' } } },
    },
  },
  plugins: [],
}
