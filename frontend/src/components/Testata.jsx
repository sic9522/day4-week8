import { Link } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { esci } from '../store/authSlice'
import Notifiche from './Notifiche'

function Tema() {
  const cambia = () => {
    const root = document.documentElement
    const esplicito = root.getAttribute('data-theme')
    const scuro = esplicito ? esplicito === 'dark' : window.matchMedia('(prefers-color-scheme: dark)').matches
    root.setAttribute('data-theme', scuro ? 'light' : 'dark')
  }
  return (
    <button className="btn piano" type="button" onClick={cambia}>
      Tema
    </button>
  )
}

/** Logo al centro, utente a destra: uguale per cliente, admin e master. */
export default function Testata({ profiloA = '/profilo' }) {
  const dispatch = useDispatch()
  const utente = useSelector((s) => s.auth.utente)
  const iniziali = utente ? `${utente.nome?.[0] ?? ''}${utente.cognome?.[0] ?? ''}`.toUpperCase() : '—'

  return (
    <header className="testata">
      <Link className="marchio" to="/">Il Tempio dei Libri</Link>
      <div className="utente">
        <Notifiche />
        <Tema />
        <button className="btn piano" type="button" onClick={() => dispatch(esci())}>Esci</button>
        <Link
          className="avatar"
          to={profiloA}
          aria-label={utente ? `${utente.nome} ${utente.cognome}, il mio profilo` : 'Il mio profilo'}
        >
          {iniziali}
        </Link>
      </div>
    </header>
  )
}
