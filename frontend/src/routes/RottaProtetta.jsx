import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useSelector } from 'react-redux'

/**
 * Chiude una rotta a chi non ha il ruolo giusto.
 *
 * È comodità per chi guarda, non sicurezza: i permessi veri stanno sui `@PreAuthorize`
 * del backend, che risponde 403 anche se qualcuno arriva all'endpoint a mano.
 */
export default function RottaProtetta({ ruoli }) {
  const { token, utente } = useSelector((s) => s.auth)
  const dove = useLocation()

  if (!token) return <Navigate to="/login" state={{ da: dove.pathname }} replace />
  if (!utente) return <p className="caricamento corpo">Un attimo…</p>
  if (ruoli && !utente.ruoli.some((r) => ruoli.includes(r))) return <Navigate to="/" replace />

  return <Outlet />
}
