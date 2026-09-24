import { useEffect, useState } from 'react'
import { libriAPI, prestitiAPI, utentiAPI, richiesteAPI, euro, dataIT, messaggioErrore } from '../../services/endpoints'

const SCONTI = [0, 10, 25, 50, 100]

/** Chiusura di un prestito: sconto sul noleggio e sulla penale, tenuti separati. */
function Chiusura({ prestito, onFatto, onErrore }) {
  const [scontoNoleggio, setScontoNoleggio] = useState(0)
  const [scontoPenale, setScontoPenale] = useState(0)

  const penale = Number(prestito.penaleMaturata ?? 0)
  const incasso = Number(prestito.costoNoleggio) * (100 - scontoNoleggio) / 100 + penale * (100 - scontoPenale) / 100

  return (
    <>
      <div className="sconti">
        <label htmlFor={`sn-${prestito.id}`}>
          Sconto noleggio
          <select id={`sn-${prestito.id}`} value={scontoNoleggio} onChange={(e) => setScontoNoleggio(Number(e.target.value))}>
            {SCONTI.map((v) => <option key={v} value={v}>{v}%</option>)}
          </select>
        </label>
        {penale > 0 && (
          <label htmlFor={`sp-${prestito.id}`}>
            Sconto penale
            <select id={`sp-${prestito.id}`} value={scontoPenale} onChange={(e) => setScontoPenale(Number(e.target.value))}>
              {SCONTI.map((v) => <option key={v} value={v}>{v}%</option>)}
            </select>
          </label>
        )}
        <span>Incasso: {euro(incasso)}</span>
      </div>

      <button
        className="btn mini"
        type="button"
        onClick={async () => {
          try {
            await prestitiAPI.chiudi({ idPrestito: prestito.id, scontoNoleggio, scontoPenale })
            onFatto()
          } catch (e) {
            onErrore(messaggioErrore(e, 'Non riesco a chiudere il prestito'))
          }
        }}
      >
        Riporta libro
      </button>
    </>
  )
}

/** Condono e annullamento chiedono il motivo: il backend lo pretende, e per una buona ragione. */
function Rettifica({ prestito, azione, etichetta, onFatto, onErrore }) {
  const [aperto, setAperto] = useState(false)
  const [motivo, setMotivo] = useState('')

  return (
    <>
      <button className="btn piano mini" type="button" onClick={() => setAperto((v) => !v)}>{etichetta}</button>
      {aperto && (
        <div className="motivo">
          <input
            id={`mot-${azione}-${prestito.id}`}
            type="text"
            placeholder="Perché? (obbligatorio)"
            value={motivo}
            onChange={(e) => setMotivo(e.target.value)}
          />
          <button
            className="btn mini"
            type="button"
            disabled={motivo.trim().length < 5}
            onClick={async () => {
              try {
                await prestitiAPI.rettifica({ idPrestito: prestito.id, azione, motivo: motivo.trim() })
                onFatto()
              } catch (e) {
                onErrore(messaggioErrore(e, 'Rettifica non riuscita'))
              }
            }}
          >
            Conferma
          </button>
        </div>
      )}
    </>
  )
}

/** Approvare una consegna apre il prestito vero: solo allora la copia esce dallo scaffale. */
function Consegna({ richiesta, onFatto, onErrore }) {
  const [rifiuto, setRifiuto] = useState(false)
  const [motivo, setMotivo] = useState('')

  async function decidi(approva) {
    try {
      await richiesteAPI.decidi({ id: richiesta.id, approva, motivo: approva ? null : motivo.trim() })
      onFatto()
    } catch (e) {
      onErrore(messaggioErrore(e, 'Non riesco a decidere la richiesta'))
    }
  }

  return (
    <div className="riga">
      <span className="tit">{richiesta.libro.titolo}</span>
      <span className="quando">{dataIT(richiesta.createdAt)}</span>
      <span className="chi">
        {richiesta.user.nome} {richiesta.user.cognome} · consegna a {richiesta.indirizzoConsegna}
      </span>
      <span className="quando">{richiesta.durata.toLowerCase()}</span>
      <div className="mani">
        <button className="btn mini" type="button" onClick={() => decidi(true)}>Approva e spedisci</button>
        <button className="btn piano mini" type="button" onClick={() => setRifiuto((v) => !v)}>Rifiuta</button>
      </div>
      {rifiuto && (
        <div className="motivo">
          <input
            id={`rif-${richiesta.id}`}
            type="text"
            placeholder="Perché? Lo leggerà il cliente"
            value={motivo}
            onChange={(e) => setMotivo(e.target.value)}
          />
          <button className="btn mini" type="button" onClick={() => decidi(false)}>Conferma il rifiuto</button>
        </div>
      )}
    </div>
  )
}

export default function PrestitiConsole() {
  const [aperti, setAperti] = useState([])
  const [daApprovare, setDaApprovare] = useState([])
  const [consegne, setConsegne] = useState([])
  const [clienti, setClienti] = useState([])
  const [libri, setLibri] = useState([])
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState(null)
  const [esito, setEsito] = useState(null)

  const [modulo, setModulo] = useState({ userId: '', libroId: '', durata: 'MEDIA', giorni: '', costoNoleggio: '' })
  const suMisura = modulo.durata === 'MISURA'

  function carica() {
    return Promise.all([
      prestitiAPI.tutti({ stato: 'APERTO', size: 60 }),
      prestitiAPI.tutti({ stato: 'IN_RITARDO', size: 60 }),
      prestitiAPI.daApprovare({ size: 60 }),
      richiesteAPI.daApprovare({ size: 60 }),
      utentiAPI.elenco({ ruolo: 'User', size: 100 }),
      libriAPI.elenco({ size: 100 }),
    ])
      .then(([a, r, d, rd, c, l]) => {
        setAperti([...r.data.content, ...a.data.content])
        setDaApprovare(d.data.content)
        setConsegne(rd.data.content)
        setClienti(c.data.content)
        setLibri(l.data.content)
        setModulo((m) => ({
          ...m,
          userId: m.userId || c.data.content[0]?.id || '',
          libroId: m.libroId || l.data.content.find((x) => x.copieDisponibili > 0)?.id || '',
        }))
      })
      .catch((e) => setErrore(messaggioErrore(e, 'Non riesco a leggere i prestiti')))
      .finally(() => setCaricamento(false))
  }

  useEffect(() => { carica() }, [])

  async function concedi(e) {
    e.preventDefault()
    setErrore(null)
    try {
      const corpo = { userId: modulo.userId, libroId: modulo.libroId }
      if (suMisura) {
        corpo.giorni = Number(modulo.giorni)
        if (modulo.costoNoleggio) corpo.costoNoleggio = Number(modulo.costoNoleggio)
      } else {
        corpo.durata = modulo.durata
      }
      const { data } = await prestitiAPI.apri(corpo)
      setEsito(`«${data.libro.titolo}» affidato a ${data.user.nome} ${data.user.cognome}, rientro entro il ${dataIT(data.dataRiconsegnaPrevista)}.`)
      await carica()
    } catch (err) {
      setErrore(messaggioErrore(err, 'Non riesco a concedere il prestito'))
    }
  }

  if (caricamento) return <p className="caricamento">Un attimo…</p>

  const inRitardo = aperti.filter((p) => p.stato === 'IN_RITARDO' && !p.richiestaRestituzione)
  const inCorso = aperti.filter((p) => p.stato === 'APERTO' && !p.richiestaRestituzione)
  const liberi = libri.filter((l) => l.copieDisponibili > 0)

  return (
    <>
      <div className="barra">
        <div>
          <p className="eyebrow">Movimenti</p>
          <h1>Prestiti</h1>
        </div>
      </div>

      {errore && <p className="errore">{errore}</p>}

      <section className="blocco">
        <div className="capo">
          <p className="eyebrow">Consegne a domicilio da approvare</p>
          <span className="pallino vi">{consegne.length}</span>
        </div>
        {consegne.length === 0 ? <p className="vuoto">Nessuna richiesta di consegna.</p> : consegne.map((r) => (
          <Consegna key={r.id} richiesta={r} onFatto={carica} onErrore={setErrore} />
        ))}
      </section>

      <section className="blocco">
        <div className="capo">
          <p className="eyebrow">Restituzioni da approvare</p>
          <span className="pallino vi">{daApprovare.length}</span>
        </div>
        {daApprovare.length === 0 ? <p className="vuoto">Nessuna restituzione in attesa.</p> : daApprovare.map((p) => (
          <div key={p.id} className="riga">
            <span className="tit">{p.libro.titolo}</span>
            <span className="totale">{euro(p.dovutoAOggi)}</span>
            <span className="chi">{p.user.nome} {p.user.cognome} · {p.user.email}</span>
            <span className="quando">scadeva il {dataIT(p.dataRiconsegnaPrevista)}</span>
            <span className="tracciato" style={{ gridColumn: '1 / -1' }}>
              {p.lettoDichiarato ? 'Dichiara di averlo letto' : 'Dichiara di non averlo letto'}
            </span>
            <div className="mani">
              <Chiusura prestito={p} onFatto={carica} onErrore={setErrore} />
            </div>
          </div>
        ))}
      </section>

      <div className="quadro">
        <article className="conto-grande dovuto">
          <p className="eyebrow">In ritardo</p>
          <b>{inRitardo.length}</b>
        </article>
        <article className="conto-grande pagato">
          <p className="eyebrow">In corso</p>
          <b>{inCorso.length}</b>
        </article>
        <article className="conto-grande">
          <p className="eyebrow">Copie a scaffale</p>
          <b>{liberi.reduce((s, l) => s + l.copieDisponibili, 0)}</b>
        </article>
      </div>

      {[['In ritardo', inRitardo, 'Nessun prestito in ritardo.'], ['In corso', inCorso, 'Nessun prestito aperto.']].map(([titolo, elenco, vuoto]) => (
        <section className="blocco" key={titolo}>
          <div className="capo">
            <p className="eyebrow">{titolo}</p>
            <span className={`pallino ${titolo === 'In ritardo' ? 'ko' : 'ok'}`}>{elenco.length}</span>
          </div>
          {elenco.length === 0 ? <p className="vuoto">{vuoto}</p> : elenco.map((p) => (
            <div key={p.id} className={`riga${p.stato === 'IN_RITARDO' ? ' tardi' : ''}`}>
              <span className="tit">{p.libro.titolo}</span>
              <span className="totale">{euro(p.dovutoAOggi)}</span>
              <span className="chi">
                {p.user.nome} {p.user.cognome} · noleggio {euro(p.costoNoleggio)}
                {p.penaleMaturata ? ` + penale ${euro(p.penaleMaturata)}` : ''}
              </span>
              <span className="quando">{dataIT(p.dataRiconsegnaPrevista)}</span>
              <div className="mani">
                <Chiusura prestito={p} onFatto={carica} onErrore={setErrore} />
                {!p.extended && (
                  <button
                    className="btn piano mini"
                    type="button"
                    onClick={async () => {
                      try {
                        await prestitiAPI.estendi({ idPrestito: p.id, giorni: 14 })
                        carica()
                      } catch (e) { setErrore(messaggioErrore(e, 'Non riesco a prorogare')) }
                    }}
                  >
                    Proroga 14 giorni
                  </button>
                )}
                {p.stato === 'IN_RITARDO' && (
                  <Rettifica prestito={p} azione="ANNULLA_RITARDO" etichetta="Condona il ritardo" onFatto={carica} onErrore={setErrore} />
                )}
              </div>
              {p.rettificaMotivo && <span className="tracciato" style={{ gridColumn: '1 / -1' }}>{p.rettificaMotivo}</span>}
            </div>
          ))}
        </section>
      ))}

      <section className="blocco">
        <p className="eyebrow">Concedi un nuovo prestito</p>
        <form onSubmit={concedi} style={{ display: 'flex', flexWrap: 'wrap', gap: 12, alignItems: 'flex-end', marginTop: 12 }}>
          <label className="campo" style={{ flex: '1 1 200px' }} htmlFor="np-cliente">
            Cliente
            <select id="np-cliente" value={modulo.userId} onChange={(e) => setModulo({ ...modulo, userId: e.target.value })}>
              {clienti.map((c) => <option key={c.id} value={c.id}>{c.cognome} {c.nome}</option>)}
            </select>
          </label>

          <label className="campo" style={{ flex: '1 1 200px' }} htmlFor="np-libro">
            Libro
            <select id="np-libro" value={modulo.libroId} onChange={(e) => setModulo({ ...modulo, libroId: e.target.value })}>
              {liberi.map((l) => <option key={l.id} value={l.id}>{l.titolo} · {l.copieDisponibili} copie</option>)}
            </select>
          </label>

          <label className="campo" style={{ flex: '1 1 160px' }} htmlFor="np-durata">
            Durata
            <select id="np-durata" value={modulo.durata} onChange={(e) => setModulo({ ...modulo, durata: e.target.value })}>
              <option value="BREVE">Breve</option>
              <option value="MEDIA">Media</option>
              <option value="LUNGA">Lunga</option>
              <option value="MISURA">Su misura…</option>
            </select>
          </label>

          {suMisura && (
            <>
              <label className="campo" style={{ flex: '1 1 120px' }} htmlFor="np-giorni">
                Giorni
                <input id="np-giorni" type="number" min={1} max={365} value={modulo.giorni} onChange={(e) => setModulo({ ...modulo, giorni: e.target.value })} required />
              </label>
              <label className="campo" style={{ flex: '1 1 140px' }} htmlFor="np-costo">
                Prezzo (vuoto = tariffa)
                <input id="np-costo" type="number" min={0} max={99.99} step={0.1} value={modulo.costoNoleggio} onChange={(e) => setModulo({ ...modulo, costoNoleggio: e.target.value })} />
              </label>
            </>
          )}

          <button className="btn" type="submit" disabled={!liberi.length}>Concedi prestito</button>
        </form>
        {esito && <p className="vuoto" style={{ marginTop: 12 }}>{esito}</p>}
      </section>
    </>
  )
}
