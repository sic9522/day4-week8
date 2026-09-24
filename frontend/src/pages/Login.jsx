import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { accedi, caricaProfilo, pulisciErrore } from '../store/authSlice'

export default function Login() {
  const dispatch = useDispatch()
  const { token, caricamento, errore } = useSelector((s) => s.auth)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')

  useEffect(() => () => { dispatch(pulisciErrore()) }, [dispatch])

  async function invia(e) {
    e.preventDefault()
    const esito = await dispatch(accedi({ username: username.trim(), password }))
    if (accedi.fulfilled.match(esito)) dispatch(caricaProfilo())
  }

  if (token) return <Navigate to="/" replace />

  return (
    <div className="ingresso">
      <div className="volume-fermo">
        <div className="facciata">
          <span className="sigla">Dal 1998</span>
          <div>
            <h1>Il Tempio dei Libri</h1>
            <p className="motto">Si entra con le proprie credenziali. Le tessere le rilascia la biblioteca.</p>
          </div>
          <span className="sigla">Ex libris</span>
        </div>

        <form className="foglio" onSubmit={invia}>
          <div>
            <h2>Accedi</h2>
            <p className="avviso">Nome utente oppure email.</p>
          </div>

          <label className="campo" htmlFor="login-utente">
            Nome utente o email
            <input
              id="login-utente"
              type="text"
              autoComplete="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </label>

          <label className="campo" htmlFor="login-password">
            Password
            <input
              id="login-password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>

          <button className="btn largo" type="submit" disabled={caricamento}>
            {caricamento ? 'Un attimo…' : 'Entra'}
          </button>

          {errore && <p className="avviso male">{errore}</p>}

          <p className="avviso">
            Non hai una tessera? <Link className="collegamento" to="/registrazione">Registrati</Link>
          </p>
        </form>
      </div>
    </div>
  )
}
