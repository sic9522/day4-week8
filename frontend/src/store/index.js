import { configureStore } from '@reduxjs/toolkit'
import authReducer from './authSlice'
import { collegaStore } from '../services/api'

const store = configureStore({
  reducer: {
    auth: authReducer,
  },
})

// L'interceptor di axios legge il token da qui: nessun modulo importa direttamente lo store
collegaStore(store)

export default store
