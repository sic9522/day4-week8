import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

// Il proxy di Vite copre solo /api: il socket punta al backend per indirizzo,
// in locale la 8080, in produzione quello di VITE_API_URL.
const BASE = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/\/$/, '')

// Pronto ma non attivo: chi lo usa chiama .activate() e si iscrive ai suoi topic.
const socket = new Client({
  webSocketFactory: () => new SockJS(`${BASE}/ws`),
})

export default socket
