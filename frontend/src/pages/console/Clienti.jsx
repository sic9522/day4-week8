import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import Tessera, { tintaDi } from '../../components/Tessera'
import Ricerca from '../../components/Ricerca'
import LibroModale, { ScenaModale } from '../../components/LibroModale'
import { utentiAPI, prestitiAPI, sediAPI, ruoliAPI, euro, dataIT, messaggioErrore } from '../../services/endpoints'

const VUOTO = { nome: '', cognome: '', email: '', username: '', password: '', dataDiNascita: '', indirizzo: '', sedeId: '' }

export default function Clienti() {
  const master = useSelector((s) => s.auth.utente?.ruoli.includes('SuperUser'))
  const [clienti, setClienti] = useState([])
  const [sedi, setSedi] = useState([])
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState(null)
  const [aperto, setAperto] = useState(null)
  const [nuovo, setNuovo] = useState(null)
  const [scheda, setScheda] = useState(null)
  const [ricerca, setRicerca] = useState({ stati: {}, vinti: null })

  function carica() {
    return utentiAPI.elenco({ ruolo: 'User', size: 100 })
      .then(({ data }) => setClienti(data.content))
      .catch((e) => setErrore(messaggioErrore(e, 'Non riesco a leggere i clienti')))
      .finally(() => setCaricamento(false))
  }

  useEffect(() => {
    carica()
    sediAPI.elenco().then(({ data }) => setSedi(data)).catch(() => {})
  }, [])

  // La scheda di un cliente vale poco senza i suoi conti: si chiedono all'apertura
  useEffect(() => {
    if (!aperto) return
    Promise.all([prestitiAPI.riepilogoDi(aperto.id), prestitiAPI.tutti({ q: aperto.email, size: 20 })])
      .then(([r, p]) => setScheda({ riepilogo: r.data, prestiti: p.data.content }))
      .catch((e) => setErrore(messaggioErrore(e, 'Non riesco a leggere la scheda')))
  }, [aperto])

  const mostrati = (ricerca.vinti ?? clienti).map((c) => ({ ...c, cerca: `${c.nome} ${c.cognome} ${c.email} ${c.username ?? ''}` }))

  if (caricamento) return <p className="caricamento">Un attimo…</p>

  return (
    <>
      <div className="barra">
        <div>
          <p className="eyebrow">Anagrafica</p>
          <h1>Clienti</h1>
        </div>
        <Ricerca
          elementi={clienti.map((c) => ({ ...c, cerca: `${c.nome} ${c.cognome} ${c.email} ${c.username ?? ''}` }))}
          campi={['cerca']}
          placeholder="Cerca un cliente"
          onRisultati={setRicerca}
        />
      </div>

      {errore && <p className="errore">{errore}</p>}

      <div className="griglia cinque">
        {mostrati.map((c, i) => (
          <Tessera
            key={c.id}
            id={`cliente-${c.id}`}
            indice={i + 3}
            titolo={`${c.nome} ${c.cognome}`}
            sotto={c.sede?.nome ?? c.username}
            stato={ricerca.stati[c.id]}
            bollo={{ classe: 'si', testo: 'Lettore' }}
            onApri={() => setAperto(c)}
          />
        ))}

        {/* Il volume ancora bianco: i clienti li iscrive un admin, mai il master */}
        {!master && (
          <button className="tessera nuova" type="button" onClick={() => setNuovo({ ...VUOTO, sedeId: sedi[0]?.id ?? '' })}>
            <span className="piu">+</span>
            <span className="che">Nuovo cliente</span>
          </button>
        )}
      </div>

      <ScenaModale>
        {aperto && (
          <LibroModale
            key={aperto.id}
            id={`cliente-${aperto.id}`}
            onChiudi={() => { setAperto(null); setScheda(null) }}
            copertina={{
              titolo: `${aperto.nome} ${aperto.cognome}`,
              sotto: aperto.username ?? aperto.email,
              tinta: tintaDi(clienti.indexOf(aperto) + 3),
              bollo: { classe: 'si', testo: 'Lettore' },
            }}
            exlibris={{
              bollo: scheda?.riepilogo?.totaleDaPagare > 0
                ? { classe: 'no', testo: 'In sospeso' }
                : { classe: 'si', testo: 'In regola' },
              dati: [
                ['Sede', aperto.sede?.nome],
                ['Iscritto dal', new Date(aperto.createdAt).toLocaleDateString('it-IT')],
                ['Prestiti', scheda?.riepilogo?.prestitiTotali],
                ['In ritardo', scheda?.riepilogo?.prestitiInRitardo],
              ],
            }}
          >
            <div>
              <h2>{aperto.nome} {aperto.cognome}</h2>
              <p className="contatto">{aperto.email}</p>
            </div>

            <div className="sezione">
              <p className="eyebrow">Prestiti aperti</p>
              {!scheda ? <p className="vuoto">Un attimo…</p>
                : scheda.prestiti.filter((p) => p.stato !== 'CHIUSO').length === 0
                  ? <p className="vuoto">Nessun prestito aperto.</p>
                  : (
                    <>
                      {scheda.prestiti.filter((p) => p.stato !== 'CHIUSO').slice(0, 4).map((p) => (
                        <div key={p.id} className="riga">
                          <span className="tit">{p.libro.titolo}</span>
                          <span className="totale">{euro(p.dovutoAOggi)}</span>
                          <span className="quando">{dataIT(p.dataRiconsegnaPrevista)}</span>
                        </div>
                      ))}
                      {scheda.prestiti.filter((p) => p.stato !== 'CHIUSO').length > 4 && (
                        <p className="ancora">
                          e altri {scheda.prestiti.filter((p) => p.stato !== 'CHIUSO').length - 4}
                        </p>
                      )}
                    </>
                  )}
            </div>

            {scheda && (
              <div className="sezione">
                <p className="eyebrow">Conti</p>
                <dl className="dati">
                  <dt>Totale pagato</dt><dd>{euro(scheda.riepilogo.totalePagato)}</dd>
                  <dt>Da pagare</dt><dd>{euro(scheda.riepilogo.totaleDaPagare)}</dd>
                </dl>
              </div>
            )}

            {master && (
              <div className="azioni">
                <button
                  className="btn"
                  type="button"
                  onClick={async () => {
                    await ruoliAPI.promuovi(aperto.id)
                    setAperto(null)
                    carica()
                  }}
                >
                  Nomina admin
                </button>
              </div>
            )}
          </LibroModale>
        )}

        {nuovo && (
          <ModuloCliente
            dati={nuovo}
            sedi={sedi}
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

function ModuloCliente({ dati, sedi, onCambia, onChiudi, onCreato, onErrore }) {
  const campo = (k) => (e) => onCambia({ ...dati, [k]: e.target.value })

  async function crea() {
    try {
      await utentiAPI.nuovoCliente(dati)
      onCreato()
    } catch (e) {
      onErrore(messaggioErrore(e, 'Non riesco a iscrivere il cliente'))
    }
  }

  return (
    <LibroModale
      id="nuovo-cliente"
      onChiudi={onChiudi}
      copertina={{
        titolo: 'Nuovo cliente',
        sotto: 'Iscrizione',
        tinta: 'linear-gradient(155deg, #262240 0%, #15121F 64%, #0A0718 100%)',
        bollo: { classe: 'vi', testo: 'Bozza' },
      }}
      exlibris={{ dati: [['Chi lo crea', 'Un admin'], ['Ruolo', 'User']] }}
    >
      <div>
        <h2>Iscrivi un cliente</h2>
        <p className="contatto">Allo sportello: chi si iscrive da solo usa la registrazione.</p>
      </div>

      <div className="sezione modulo-fitto due">
        <label className="campo" htmlFor="nc-nome">Nome<input id="nc-nome" value={dati.nome} onChange={campo('nome')} /></label>
        <label className="campo" htmlFor="nc-cognome">Cognome<input id="nc-cognome" value={dati.cognome} onChange={campo('cognome')} /></label>
        <label className="campo" htmlFor="nc-email">Email<input id="nc-email" type="email" value={dati.email} onChange={campo('email')} /></label>
        <label className="campo" htmlFor="nc-username">Nome utente<input id="nc-username" value={dati.username} onChange={campo('username')} /></label>
        <label className="campo" htmlFor="nc-password">Password<input id="nc-password" type="password" value={dati.password} onChange={campo('password')} /></label>
        <label className="campo" htmlFor="nc-nascita">Data di nascita<input id="nc-nascita" type="date" value={dati.dataDiNascita} onChange={campo('dataDiNascita')} /></label>
        <label className="campo" htmlFor="nc-indirizzo">Indirizzo<input id="nc-indirizzo" value={dati.indirizzo} onChange={campo('indirizzo')} /></label>
        <label className="campo" htmlFor="nc-sede">Sede
          <select id="nc-sede" value={dati.sedeId} onChange={campo('sedeId')}>
            {sedi.map((s) => <option key={s.id} value={s.id}>{s.etichetta}</option>)}
          </select>
        </label>
      </div>

      <div className="azioni">
        <button className="btn" type="button" onClick={crea}>Iscrivi cliente</button>
      </div>
    </LibroModale>
  )
}
