/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './app/**/*.{js,ts,jsx,tsx,mdx}',
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          green: '#00A86B',
          darkGreen: '#046A4E',
          lightGreen: '#10B981',
          mint: '#DCFCE7',
          softMint: '#F0FDF4',
          coral: '#EF4444',
          gold: '#D97706',
          goldLight: '#FEF3C7',
        },
      },
    },
  },
  plugins: [],
};
