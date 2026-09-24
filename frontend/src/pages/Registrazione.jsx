import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { sediAPI, utentiAPI, messaggioErrore } from '../services/endpoints'

/** Registrazione aperta, ma solo per i lettori: gli admin li nomina il master. */
export default function Registrazione() {
  const vai = useNavigate()
  const [sedi, setSedi] = useState([])
  const [dati, setDati] = useState({
    nome: '', cognome: '', email: '', username: '', password: '',
    dataDiNascita: '', indirizzo: '', sedeId: '',
  })
  const [errore, setErrore] = useState(null)
  const [invio, setInvio] = useState(false)

  useEffect(() => {
    sediAPI.elenco()
      .then(({ data }) => {
        setSedi(data)
        if (data.length) setDati((d) => ({ ...d, sedeId: data[0].id }))
      })
      .catch((e) => setErrore(messaggioErrore(e, 'Non riesco a leggere le sedi')))
  }, [])

  const cambia = (campo) => (e) => setDati((d) => ({ ...d, [campo]: e.target.value }))

  async function invia(e) {
    e.preventDefault()
    setErrore(null)
    setInvio(true)
    try {
      await utentiAPI.registra(dati)
      vai('/login', { replace: true, state: { registrato: true } })
    } catch (err) {
      setErrore(messaggioErrore(err, 'Registrazione non riuscita'))
    } finally {
      setInvio(false)
    }
  }

  return (
    <div className="ingresso">
      <div className="volume-fermo">
        <div className="facciata">
          <span className="sigla">Nuova tessera</span>
          <div>
            <h1>Iscriviti</h1>
            <p className="motto">Scegli la sede dove ritirerai i libri: è lì che ti aspetteranno.</p>
          </div>
          <span className="sigla">Ex libris</span>
        </div>

        <form className="foglio" onSubmit={invia}>
          <div>
            <h2>Registrati</h2>
            <p className="avviso">Solo per i lettori.</p>
          </div>

          <div className="coppia">
            <label className="campo" htmlFor="reg-nome">
              Nome
              <input id="reg-nome" type="text" autoComplete="given-name" value={dati.nome} onChange={cambia('nome')} required />
            </label>
            <label className="campo" htmlFor="reg-cognome">
              Cognome
              <input id="reg-cognome" type="text" autoComplete="family-name" value={dati.cognome} onChange={cambia('cognome')} required />
            </label>
          </div>

          <label className="campo" htmlFor="reg-email">
            Email
            <input id="reg-email" type="email" autoComplete="email" value={dati.email} onChange={cambia('email')} required />
          </label>

          <div className="coppia">
            <label className="campo" htmlFor="reg-username">
              Nome utente
              <input id="reg-username" type="text" autoComplete="username" minLength={3} value={dati.username} onChange={cambia('username')} required />
            </label>
            <label className="campo" htmlFor="reg-password">
              Password
              <input id="reg-password" type="password" autoComplete="new-password" minLength={8} value={dati.password} onChange={cambia('password')} required />
            </label>
          </div>

          <div className="coppia">
            <label className="campo" htmlFor="reg-nascita">
              Data di nascita
              <input id="reg-nascita" type="date" value={dati.dataDiNascita} onChange={cambia('dataDiNascita')} required />
            </label>
            <label className="campo" htmlFor="reg-indirizzo">
              Indirizzo
              <input id="reg-indirizzo" type="text" autoComplete="street-address" value={dati.indirizzo} onChange={cambia('indirizzo')} required />
            </label>
          </div>

          <label className="campo" htmlFor="reg-sede">
            Sede
            <select id="reg-sede" value={dati.sedeId} onChange={cambia('sedeId')} required>
              {sedi.map((s) => <option key={s.id} value={s.id}>{s.etichetta}</option>)}
            </select>
          </label>

          <button className="btn largo" type="submit" disabled={invio || !sedi.length}>
            {invio ? 'Un attimo…' : 'Crea la tessera'}
          </button>

          {errore && <p className="avviso male">{errore}</p>}

          <p className="avviso">
            Hai già una tessera? <Link className="collegamento" to="/login">Accedi</Link>
          </p>
        </form>
      </div>
    </div>
  )
}
