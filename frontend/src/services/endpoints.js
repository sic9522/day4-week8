import api from './api'

// Un unico posto dove stanno i percorsi: se il backend cambia rotta, si tocca solo qui.

export const utentiAPI = {
  registra: (dati) => api.post('/api/user/register', dati),
  nuovoCliente: (dati) => api.post('/api/user/newCliente', dati),
  nuovoAdmin: (dati) => api.post('/api/user/newAdmin', dati),
  elenco: (params) => api.get('/api/user/all', { params }),
  dettaglio: (id) => api.get(`/api/user/${id}`),
}

export const ruoliAPI = {
  promuovi: (userId) => api.post(`/api/role/grantAdmin/${userId}`),
  revoca: (userId) => api.delete(`/api/role/revokeAdmin/${userId}`),
}

export const sediAPI = {
  elenco: () => api.get('/api/sedi'),
}

export const libriAPI = {
  elenco: (params) => api.get('/api/book/all', { params }),
  cerca: (params) => api.post('/api/book/search', null, { params }),
  nuovo: (dati) => api.post('/api/book/newLibro', dati),
  aggiungiCopie: (dati) => api.patch('/api/book/addLibro', dati),
}

export const generiAPI = {
  elenco: () => api.get('/api/generi/allGeneri'),
  nuovo: (dati) => api.post('/api/generi/newGenere', dati),
}

export const prestitiAPI = {
  miei: (params) => api.get('/api/prestiti/UserPrestiti', { params }),
  tutti: (params) => api.get('/api/prestiti/AllPrestiti', { params }),
  perLibro: (libroId) => api.get(`/api/prestiti/PerLibro/${libroId}`),
  daApprovare: (params) => api.get('/api/prestiti/DaApprovare', { params }),
  apri: (dati) => api.post('/api/prestiti/NewPrestito', dati),
  chiudi: (dati) => api.patch('/api/prestiti/ClosePrestito', dati),
  estendi: (dati) => api.patch('/api/prestiti/ExtendPrestito', dati),
  chiediRestituzione: (dati) => api.patch('/api/prestiti/RichiediRestituzione', dati),
  rettifica: (dati) => api.patch('/api/prestiti/Rettifica', dati),
  riepilogo: () => api.get('/api/prestiti/Riepilogo'),
  riepilogoDi: (userId) => api.get(`/api/prestiti/Riepilogo/${userId}`),
}

export const richiesteAPI = {
  apri: (dati) => api.post('/api/richieste', dati),
  mie: (params) => api.get('/api/richieste/mie', { params }),
  daApprovare: (params) => api.get('/api/richieste/daApprovare', { params }),
  decidi: (dati) => api.patch('/api/richieste/decidi', dati),
}

export const notificheAPI = {
  mie: (params) => api.get('/api/notifiche/mie', { params }),
  daLeggere: () => api.get('/api/notifiche/daLeggere'),
  lette: () => api.patch('/api/notifiche/lette'),
}

export const preferitiAPI = {
  miei: (params) => api.get('/api/preferiti/miei', { params }),
  aggiungi: (libroId) => api.post('/api/preferiti', { libroId }),
  rimuovi: (libroId) => api.delete(`/api/preferiti/${libroId}`),
  segnaLetto: (libroId, letto) => api.patch('/api/preferiti/letto', { libroId, letto }),
}

export const segnalazioniAPI = {
  mie: (params) => api.get('/api/segnalazioni/mie', { params }),
  tutte: (params) => api.get('/api/segnalazioni/all', { params }),
  apri: (dati) => api.post('/api/segnalazioni', dati),
  rispondi: (dati) => api.patch('/api/segnalazioni/rispondi', dati),
}

export const costantiAPI = {
  tariffe: () => api.get('/api/costanti/tariffe'),
  tutte: () => api.get('/api/costanti/all'),
  modifica: (dati) => api.patch('/api/costanti/editCostante', dati),
}

// ---------- formattazioni comuni ----------

export function euro(n) {
  const v = Number(n ?? 0)
  return `€ ${v.toFixed(2).replace('.', ',')}`
}

export function dataIT(iso) {
  if (!iso) return '—'
  const [a, m, g] = String(iso).slice(0, 10).split('-')
  return `${g}/${m}/${a}`
}

export function messaggioErrore(e, fallback = 'Qualcosa non ha funzionato') {
  return e?.response?.data?.message ?? e?.response?.data?.error ?? fallback
}
