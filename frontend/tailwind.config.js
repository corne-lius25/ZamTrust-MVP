/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'ui-monospace', 'SFMono-Regular', 'monospace'],
      },
      colors: {
        ink: {
          DEFAULT: '#0a0a0a',
          secondary: '#404040',
          muted: '#737373',
          subtle: '#a3a3a3',
        },
        surface: {
          DEFAULT: '#ffffff',
          subtle: '#fafafa',
          muted: '#f4f4f5',
        },
        border: {
          DEFAULT: '#e5e5e5',
          strong: '#d4d4d4',
          subtle: '#f0f0f0',
        },
        accent: {
          DEFAULT: '#1e63f5',
          hover: '#194de1',
          subtle: '#eef6ff',
        },
        success: {
          DEFAULT: '#16a34a',
          subtle: '#f0fdf4',
        },
        warning: {
          DEFAULT: '#ca8a04',
          subtle: '#fefce8',
        },
        danger: {
          DEFAULT: '#dc2626',
          subtle: '#fef2f2',
        },
      },
      fontSize: {
        'display': ['4.5rem', { lineHeight: '1.05', letterSpacing: '-0.03em', fontWeight: '600' }],
        'h1':      ['3.5rem', { lineHeight: '1.1',  letterSpacing: '-0.025em', fontWeight: '600' }],
        'h2':      ['2.5rem', { lineHeight: '1.15', letterSpacing: '-0.02em',  fontWeight: '600' }],
        'h3':      ['1.75rem',{ lineHeight: '1.2',  letterSpacing: '-0.015em', fontWeight: '600' }],
        'h4':      ['1.25rem',{ lineHeight: '1.3',  letterSpacing: '-0.01em',  fontWeight: '600' }],
        'body-lg': ['1.125rem', { lineHeight: '1.6' }],
        'body':    ['1rem',     { lineHeight: '1.6' }],
        'small':   ['0.875rem', { lineHeight: '1.5' }],
        'caption': ['0.75rem',  { lineHeight: '1.4', letterSpacing: '0.02em', fontWeight: '500' }],
      },
      borderRadius: {
        'input': '4px',
        'btn':   '6px',
        'card':  '8px',
      },
      boxShadow: {
        'xs': '0 1px 2px rgba(0,0,0,0.04)',
        'sm': '0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)',
      },
      transitionTimingFunction: {
        'natural': 'cubic-bezier(0.16, 1, 0.3, 1)',
      },
      maxWidth: {
        'content': '1152px',
        'wide':    '1280px',
      },
    },
  },
  plugins: [],
}
