import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Every fetch("/api/...") from React is forwarded to Spring Boot.
    // Same origin for the browser = no CORS problems and the login
    // session cookie is sent automatically.
    proxy: {
      '/api': 'http://localhost:8081'
    }
  }
})
