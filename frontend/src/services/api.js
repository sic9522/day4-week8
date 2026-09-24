import axios from 'axios'

// In sviluppo baseURL e' vuota e il proxy di Vite inoltra /api alla 8080.
// In produzione arriva da VITE_API_URL, iniettata durante la build.
const BASE = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '')

const api = axios.create({ baseURL: BASE })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const indirizzo = BASE || '(stessa origine, proxy di Vite)'

export const statoAPI = {
  leggi: () => api.get('/api/stato'),
}

export default api
