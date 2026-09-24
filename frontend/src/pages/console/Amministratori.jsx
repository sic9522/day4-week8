import { useEffect, useState } from 'react'
import Tessera, { tintaDi } from '../../components/Tessera'
import Ricerca from '../../components/Ricerca'
import LibroModale, { ScenaModale } from '../../components/LibroModale'
import { utentiAPI, ruoliAPI, messaggioErrore } from '../../services/endpoints'

const VUOTO = {
  nome: '', cognome: '', email: '', username: '', password: '', dataDiNascita: '', indirizzo: '',
  nomeNegozio: '', via: '', citta: 'Roma', cap: '',
}

/** Solo il master arriva qui: è l'unico che può nominare e revocare gli admin. */
export default function Amministratori() {
  const [admin, setAdmin] = useState([])
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState(null)
  const [aperto, setAperto] = useState(null)
  const [nuovo, setNuovo] = useState(null)
  const [ricerca, setRicerca] = useState({ stati: {}, vinti: null })

  function carica() {
    return utentiAPI.elenco({ ruolo: 'Admin', size: 100 })
      .then(({ data }) => setAdmin(data.content))
      .catch((e) => setErrore(messaggioErrore(e, 'Non riesco a leggere gli admin')))
      .finally(() => setCaricamento(false))
  }

  useEffect(() => { carica() }, [])

  const conCerca = admin.map((a) => ({ ...a, cerca: `${a.nome} ${a.cognome} ${a.email} ${a.sede?.nome ?? ''}` }))
  const mostrati = ricerca.vinti ?? conCerca

  if (caricamento) return <p className="caricamento">Un attimo…</p>

  return (
    <>
      <div className="barra">
        <div>
          <p className="eyebrow">Gestione</p>
          <h1>Admin</h1>
        </div>
        <Ricerca elementi={conCerca} campi={['cerca']} placeholder="Cerca un admin" onRisultati={setRicerca} />
      </div>

      {errore && <p className="errore">{errore}</p>}

      <div className="griglia cinque">
        {mostrati.map((a, i) => (
          <Tessera
            key={a.id}
            id={`admin-${a.id}`}
            indice={i}
            titolo={`${a.nome} ${a.cognome}`}
            sotto={a.sede?.nome ?? 'Nessuna sede'}
            stato={ricerca.stati[a.id]}
            bollo={{ classe: 'vi', testo: 'Admin' }}
            onApri={() => setAperto(a)}
          />
        ))}

        <button className="tessera nuova" type="button" onClick={() => setNuovo(VUOTO)}>
          <span className="piu">+</span>
          <span className="che">Nuovo admin</span>
        </button>
      </div>

      <ScenaModale>
        {aperto && (
          <LibroModale
            key={aperto.id}
            id={`admin-${aperto.id}`}
            onChiudi={() => setAperto(null)}
            copertina={{
              titolo: `${aperto.nome} ${aperto.cognome}`,
              sotto: aperto.sede?.nome ?? 'Nessuna sede',
              tinta: tintaDi(admin.indexOf(aperto)),
              bollo: { classe: 'vi', testo: 'Admin' },
            }}
            exlibris={{
              bollo: { classe: 'vi', testo: 'Admin' },
              dati: [
                ['Sede', aperto.sede?.nome],
                ['Via', aperto.sede?.via],
                ['Utente', aperto.username],
                ['Admin dal', new Date(aperto.createdAt).toLocaleDateString('it-IT')],
              ],
            }}
          >
            <div>
              <h2>{aperto.nome} {aperto.cognome}</h2>
              <p className="contatto">{aperto.email}</p>
            </div>

            <div className="sezione">
              <p className="eyebrow">Sede che gestisce</p>
              <p className="sinossi">{aperto.sede?.etichetta ?? 'Nessuna sede assegnata'}</p>
            </div>

            <div className="azioni">
              <button
                className="btn rischio"
                type="button"
                onClick={async () => {
                  try {
                    await ruoliAPI.revoca(aperto.id)
                    setAperto(null)
                    carica()
                  } catch (e) {
                    setErrore(messaggioErrore(e, 'Non riesco a revocare il ruolo'))
                  }
                }}
              >
                Revoca admin
              </button>
            </div>
          </LibroModale>
        )}

        {nuovo && (
          <ModuloAdmin
            dati={nuovo}
            onCambia={setNuovo}
            onChiudi={() => setNuovo(null)}
            onCreato={() => { setNuovo(null); carica() }}
            onErrore={setErrore}
          />
        )}
      </ScenaModale>
    </>
  )
}

function ModuloAdmin({ dati, onCambia, onChiudi, onCreato, onErrore }) {
  const campo = (k) => (e) => onCambia({ ...dati, [k]: e.target.value })

  async function crea() {
    try {
      await utentiAPI.nuovoAdmin(dati)
      onCreato()
    } catch (e) {
      onErrore(messaggioErrore(e, 'Non riesco a nominare l’admin'))
    }
  }

  return (
    <LibroModale
      id="nuovo-admin"
      onChiudi={onChiudi}
      copertina={{
        titolo: 'Nuovo admin',
        sotto: 'Nomina',
        tinta: 'linear-gradient(155deg, #262240 0%, #15121F 64%, #0A0718 100%)',
        bollo: { classe: 'vi', testo: 'Bozza' },
      }}
      exlibris={{ dati: [['Chi lo crea', 'Il master'], ['Ruolo', 'Admin']] }}
    >
      <div>
        <h2>Nomina un amministratore</h2>
        <p className="contatto">Indica anche la sede che gestirà: se non esiste, viene creata.</p>
      </div>

      <div className="sezione modulo-fitto due">
        <label className="campo" htmlFor="na-nome">Nome<input id="na-nome" value={dati.nome} onChange={campo('nome')} /></label>
        <label className="campo" htmlFor="na-cognome">Cognome<input id="na-cognome" value={dati.cognome} onChange={campo('cognome')} /></label>
        <label className="campo" htmlFor="na-email">Email<input id="na-email" type="email" value={dati.email} onChange={campo('email')} /></label>
        <label className="campo" htmlFor="na-username">Nome utente<input id="na-username" value={dati.username} onChange={campo('username')} /></label>
        <label className="campo" htmlFor="na-password">Password iniziale<input id="na-password" type="password" value={dati.password} onChange={campo('password')} /></label>
        <label className="campo" htmlFor="na-nascita">Data di nascita<input id="na-nascita" type="date" value={dati.dataDiNascita} onChange={campo('dataDiNascita')} /></label>
        <label className="campo" htmlFor="na-indirizzo">Indirizzo<input id="na-indirizzo" value={dati.indirizzo} onChange={campo('indirizzo')} /></label>
        <label className="campo" htmlFor="na-negozio">Nome negozio<input id="na-negozio" value={dati.nomeNegozio} onChange={campo('nomeNegozio')} /></label>
        <label className="campo" htmlFor="na-via">Via<input id="na-via" value={dati.via} onChange={campo('via')} /></label>
        <div className="coppia">
          <label className="campo" htmlFor="na-citta">Città<input id="na-citta" value={dati.citta} onChange={campo('citta')} /></label>
          <label className="campo" htmlFor="na-cap">CAP<input id="na-cap" inputMode="numeric" maxLength={5} value={dati.cap} onChange={campo('cap')} /></label>
        </div>
      </div>

      <div className="azioni">
        <button className="btn" type="button" onClick={crea}>Nomina admin</button>
      </div>
    </LibroModale>
  )
}
