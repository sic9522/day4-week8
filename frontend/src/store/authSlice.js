import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import api from '../services/api'

// Il token vive qui: è Redux la fonte di verità, non localStorage.
// In localStorage resta solo una copia per sopravvivere al refresh della pagina.
const CHIAVE = 'tempio.auth'

function leggiSalvato() {
  try {
    const grezzo = localStorage.getItem(CHIAVE)
    return grezzo ? JSON.parse(grezzo) : null
  } catch {
    return null
  }
}

function salva(dati) {
  try {
    if (dati) localStorage.setItem(CHIAVE, JSON.stringify(dati))
    else localStorage.removeItem(CHIAVE)
  } catch {
    // finestra anonima o storage bloccato: la sessione vale solo per questa scheda
  }
}

const salvato = leggiSalvato()

const statoIniziale = {
  token: salvato?.token ?? null,
  scadenza: salvato?.scadenza ?? null,
  utente: null,
  caricamento: false,
  errore: null,
}

export const accedi = createAsyncThunk(
  'auth/accedi',
  async ({ username, password }, { rejectWithValue }) => {
    try {
      const { data } = await api.post('/api/user/login', { username, password })
      return data
    } catch (e) {
      return rejectWithValue(messaggio(e, 'Credenziali non valide'))
    }
  },
)

export const caricaProfilo = createAsyncThunk('auth/me', async (_, { rejectWithValue }) => {
  try {
    const { data } = await api.get('/api/user/me')
    return data
  } catch (e) {
    return rejectWithValue(messaggio(e, 'Sessione non valida'))
  }
})

// Revoca il token attuale e ne ottiene uno nuovo: il vecchio smette di funzionare subito
export const rinnova = createAsyncThunk('auth/refresh', async (_, { rejectWithValue }) => {
  try {
    const { data } = await api.post('/api/user/refresh')
    return data
  } catch (e) {
    return rejectWithValue(messaggio(e, 'Sessione scaduta'))
  }
})

// Il logout va chiesto al server: il token viene revocato a database, non solo dimenticato
export const esci = createAsyncThunk('auth/logout', async () => {
  try {
    await api.post('/api/user/logout')
  } catch {
    // se la chiamata fallisce si esce lo stesso: il token locale va comunque buttato
  }
})

function messaggio(errore, fallback) {
  return errore?.response?.data?.message ?? errore?.response?.data?.error ?? fallback
}

const authSlice = createSlice({
  name: 'auth',
  initialState: statoIniziale,
  reducers: {
    // Usata dall'interceptor quando il server risponde 401: la sessione non è più valida
    sessioneScaduta(state) {
      state.token = null
      state.scadenza = null
      state.utente = null
      salva(null)
    },
    pulisciErrore(state) {
      state.errore = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(accedi.pending, (state) => {
        state.caricamento = true
        state.errore = null
      })
      .addCase(accedi.fulfilled, (state, action) => {
        state.caricamento = false
        state.token = action.payload.token
        state.scadenza = action.payload.expiresAt
        salva({ token: state.token, scadenza: state.scadenza })
      })
      .addCase(accedi.rejected, (state, action) => {
        state.caricamento = false
        state.errore = action.payload ?? 'Accesso non riuscito'
      })
      .addCase(caricaProfilo.fulfilled, (state, action) => {
        state.utente = action.payload
      })
      .addCase(caricaProfilo.rejected, (state) => {
        state.token = null
        state.scadenza = null
        state.utente = null
        salva(null)
      })
      .addCase(rinnova.fulfilled, (state, action) => {
        state.token = action.payload.token
        state.scadenza = action.payload.expiresAt
        salva({ token: state.token, scadenza: state.scadenza })
      })
      .addCase(esci.fulfilled, (state) => {
        state.token = null
        state.scadenza = null
        state.utente = null
        salva(null)
      })
  },
})

export const { sessioneScaduta, pulisciErrore } = authSlice.actions

// I ruoli del backend sono SuperUser, Admin, User
export const selezionaRuoli = (state) => state.auth.utente?.ruoli ?? []
export const eMaster = (state) => selezionaRuoli(state).includes('SuperUser')
export const eAdmin = (state) => selezionaRuoli(state).some((r) => r === 'Admin' || r === 'SuperUser')
export const eAutenticato = (state) => Boolean(state.auth.token)

export default authSlice.reducer
