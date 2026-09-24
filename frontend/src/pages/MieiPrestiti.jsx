import { useEffect, useState } from 'react'
import { prestitiAPI, segnalazioniAPI, richiesteAPI, libriAPI, euro, dataIT, messaggioErrore } from '../services/endpoints'

function Segnalazione({ prestito, onFatto }) {
  const [aperto, setAperto] = useState(false)
  const [testo, setTesto] = useState('')
  const [inviato, setInviato] = useState(false)

  async function invia() {
    if (testo.trim().length < 5) return
    await segnalazioniAPI.apri({ idPrestito: prestito.id, testo: testo.trim() })
    setInviato(true)
    onFatto?.()
  }

  if (inviato) return <p className="vuoto">Segnalazione inviata. Un bibliotecario ti risponderà.</p>

  return (
    <>
      <button className="btn piano mini" type="button" onClick={() => setAperto((v) => !v)}>
        Segnala un problema
      </button>
      {aperto && (
        <div className="motivo">
          <input
            id={`segnala-${prestito.id}`}
            type="text"
            placeholder="Copia rovinata, pagine mancanti, ritardo non tuo…"
            value={testo}
            onChange={(e) => setTesto(e.target.value)}
          />
          <button className="btn mini" type="button" onClick={invia}>Invia</button>
        </div>
      )}
    </>
  )
}

export default function MieiPrestiti() {
  const [prestiti, setPrestiti] = useState([])
  const [riepilogo, setRiepilogo] = useState(null)
  const [richieste, setRichieste] = useState([])
  const [libri, setLibri] = useState([])
  const [nuova, setNuova] = useState({ libroId: '', durata: 'MEDIA', indirizzoConsegna: '' })
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState(null)

  function carica() {
    return Promise.all([
      prestitiAPI.miei({ size: 60 }),
      prestitiAPI.riepilogo(),
      richiesteAPI.mie({ size: 30 }),
      libriAPI.elenco({ size: 100 }),
    ])
      .then(([p, r, q, l]) => {
        setPrestiti(p.data.content)
        setRiepilogo(r.data)
        setRichieste(q.data.content)
        setLibri(l.data.content)
        setNuova((n) => ({ ...n, libroId: n.libroId || l.data.content.find((x) => x.copieDisponibili > 0)?.id || '' }))
      })
      .catch((e) => setErrore(messaggioErrore(e, 'Non riesco a leggere i tuoi prestiti')))
      .finally(() => setCaricamento(false))
  }

  useEffect(() => { carica() }, [])

  // La restituzione si chiede: il prestito resta aperto finché un bibliotecario non approva
  async function chiediRestituzione(p, letto) {
    try {
      await prestitiAPI.chiediRestituzione({ idPrestito: p.id, letto })
      await carica()
    } catch (e) {
      setErrore(messaggioErrore(e, 'Non riesco a mandare la richiesta'))
    }
  }

  if (caricamento) return <p className="caricamento">Un attimo…</p>

  const aperti = prestiti.filter((p) => p.stato !== 'CHIUSO')
  const chiusi = prestiti.filter((p) => p.stato === 'CHIUSO')

  return (
    <>
      <div className="barra">
        <div>
          <p className="eyebrow">Movimenti</p>
          <h1>I miei prestiti</h1>
        </div>
      </div>

      {errore && <p className="errore">{errore}</p>}

      {riepilogo && (
        <div className="quadro">
          <article className="conto-grande pagato">
            <p className="eyebrow">Totale pagato</p>
            <b>{euro(riepilogo.totalePagato)}</b>
          </article>
          <article className="conto-grande dovuto">
            <p className="eyebrow">Da pagare</p>
            <b>{euro(riepilogo.totaleDaPagare)}</b>
          </article>
          <article className="conto-grande">
            <p className="eyebrow">Prestiti aperti</p>
            <b>{riepilogo.prestitiAperti}</b>
          </article>
        </div>
      )}

      <section className="blocco">
        <p className="eyebrow">In corso</p>
        {aperti.length === 0 ? (
          <p className="vuoto">Non hai prestiti aperti.</p>
        ) : aperti.map((p) => (
          <div key={p.id} className={`riga${p.stato === 'IN_RITARDO' ? ' tardi' : ''}`}>
            <span className="tit">{p.libro.titolo}</span>
            <span className="totale">{euro(p.dovutoAOggi)}</span>
            <span className="chi">
              Noleggio {euro(p.costoNoleggio)}
              {p.penaleMaturata ? ` + penale maturata ${euro(p.penaleMaturata)}` : ' · nessuna penale'}
            </span>
            <span className="quando">
              {p.stato === 'IN_RITARDO' ? 'scaduto il ' : 'scade il '}
              {dataIT(p.dataRiconsegnaPrevista)}
            </span>
            <div className="mani">
              {p.richiestaRestituzione ? (
                <span className="attesa">
                  Restituzione chiesta · {p.lettoDichiarato ? 'dichiarato letto' : 'dichiarato non letto'} · aspetta l'approvazione
                </span>
              ) : (
                <>
                  <button className="btn mini" type="button" onClick={() => chiediRestituzione(p, true)}>
                    Chiedi la restituzione · letto
                  </button>
                  <button className="btn piano mini" type="button" onClick={() => chiediRestituzione(p, false)}>
                    · non letto
                  </button>
                </>
              )}
              <Segnalazione prestito={p} />
            </div>
          </div>
        ))}
      </section>

      <section className="blocco">
        <p className="eyebrow">Richieste a domicilio</p>
        {richieste.length === 0 ? (
          <p className="vuoto">Non hai richieste. Scegli un libro qui sotto e te lo portiamo a casa.</p>
        ) : richieste.map((r) => (
          <div key={r.id} className="riga">
            <span className="tit">{r.libro.titolo}</span>
            <span className="quando">{dataIT(r.createdAt)}</span>
            <span className="chi">Consegna a {r.indirizzoConsegna}</span>
            <div className="mani">
              {r.stato === 'IN_ATTESA' && <span className="attesa">Aspetta l&apos;approvazione</span>}
              {r.stato === 'APPROVATA' && <span className="attesa">Approvata · in arrivo</span>}
              {r.stato === 'RIFIUTATA' && (
                <span className="attesa" style={{ background: 'var(--ko-bg)', color: 'var(--ko-fg)' }}>
                  Rifiutata{r.motivo ? ` · ${r.motivo}` : ''}
                </span>
              )}
            </div>
          </div>
        ))}

        <form
          style={{ display: 'flex', flexWrap: 'wrap', gap: 12, alignItems: 'flex-end', marginTop: 16 }}
          onSubmit={async (e) => {
            e.preventDefault()
            try {
              await richiesteAPI.apri(nuova)
              setNuova({ ...nuova, indirizzoConsegna: '' })
              await carica()
            } catch (err) {
              setErrore(messaggioErrore(err, 'Non riesco a mandare la richiesta'))
            }
          }}
        >
          <label className="campo" style={{ flex: '1 1 220px' }} htmlFor="ric-libro">
            Libro
            <select id="ric-libro" value={nuova.libroId} onChange={(e) => setNuova({ ...nuova, libroId: e.target.value })}>
              {libri.filter((l) => l.copieDisponibili > 0).map((l) => (
                <option key={l.id} value={l.id}>{l.titolo} · {l.autore}</option>
              ))}
            </select>
          </label>
          <label className="campo" style={{ flex: '0 1 150px' }} htmlFor="ric-durata">
            Durata
            <select id="ric-durata" value={nuova.durata} onChange={(e) => setNuova({ ...nuova, durata: e.target.value })}>
              <option value="BREVE">Breve</option>
              <option value="MEDIA">Media</option>
              <option value="LUNGA">Lunga</option>
            </select>
          </label>
          <label className="campo" style={{ flex: '1 1 220px' }} htmlFor="ric-indirizzo">
            Indirizzo (vuoto = il tuo)
            <input id="ric-indirizzo" value={nuova.indirizzoConsegna} onChange={(e) => setNuova({ ...nuova, indirizzoConsegna: e.target.value })} />
          </label>
          <button className="btn" type="submit" disabled={!nuova.libroId}>Chiedila a casa</button>
        </form>
      </section>

      <section className="blocco">
        <p className="eyebrow">Storico</p>
        {chiusi.length === 0 ? (
          <p className="vuoto">Nessun prestito concluso.</p>
        ) : chiusi.map((p) => (
          <div key={p.id} className="riga">
            <span className="tit">{p.libro.titolo}</span>
            <span className="totale">{euro(p.totalePagato)}</span>
            <span className="chi">
              Noleggio {euro(p.costoNoleggio)}
              {p.penaleRiscossa ? ` + penale ${euro(p.penaleRiscossa)}` : ''}
              {p.scontoNoleggio ? ` − sconto ${p.scontoNoleggio}%` : ''}
            </span>
            <span className="quando">riconsegnato il {dataIT(p.dataRiconsegnaEffettiva)}</span>
          </div>
        ))}
      </section>
    </>
  )
}
