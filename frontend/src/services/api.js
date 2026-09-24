import axios from 'axios'

// In sviluppo baseURL e' vuota e il proxy di Vite inoltra /api alla 8080.
// In produzione arriva da VITE_API_URL, iniettata durante la build.
const BASE = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '')

const api = axios.create({ baseURL: BASE })

// Lo store viene collegato all'avvio da store/index.js: così api.js non importa lo store
// e lo store non importa api.js in modo circolare.
let store = null
export function collegaStore(s) {
  store = s
}

api.interceptors.request.use((config) => {
  const token = store?.getState().auth.token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (risposta) => risposta,
  (errore) => {
    // 401 significa token scaduto o revocato lato server: la sessione locale non vale più.
    // Si spedisce il tipo dell'azione invece di importarla, per non creare un ciclo con lo store.
    if (errore?.response?.status === 401 && store) {
      store.dispatch({ type: 'auth/sessioneScaduta' })
    }
    return Promise.reject(errore)
  },
)

export const indirizzo = BASE || '(stessa origine, proxy di Vite)'

export const statoAPI = {
  leggi: () => api.get('/api/stato'),
}

export default api
