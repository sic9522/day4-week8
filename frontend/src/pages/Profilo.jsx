import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { esci } from '../store/authSlice'
import { prestitiAPI, preferitiAPI, euro, messaggioErrore } from '../services/endpoints'

const ETICHETTE = { SuperUser: 'Master', Admin: 'Amministratore', User: 'Lettore' }

export default function Profilo() {
  const dispatch = useDispatch()
  const utente = useSelector((s) => s.auth.utente)
  const [riepilogo, setRiepilogo] = useState(null)
  const [libri, setLibri] = useState(null)
  const [errore, setErrore] = useState(null)

  const lettore = utente?.ruoli.includes('User')

  useEffect(() => {
    if (!lettore) return
    Promise.all([prestitiAPI.riepilogo(), preferitiAPI.miei({ size: 100 })])
      .then(([r, p]) => { setRiepilogo(r.data); setLibri(p.data.content) })
      .catch((e) => setErrore(messaggioErrore(e, 'Non riesco a leggere i tuoi conti')))
  }, [lettore])

  if (!utente) return <p className="caricamento">Un attimo…</p>

  return (
    <>
      <div className="barra">
        <div>
          <p className="eyebrow">{utente.ruoli.map((r) => ETICHETTE[r] ?? r).join(' · ')}</p>
          <h1>{utente.nome} {utente.cognome}</h1>
        </div>
        <button className="btn piano" type="button" onClick={() => dispatch(esci())}>Esci</button>
      </div>

      {errore && <p className="errore">{errore}</p>}

      <section className="blocco">
        <p className="eyebrow">Anagrafica</p>
        <dl className="dati">
          <dt>Nome utente</dt><dd>{utente.username ?? '—'}</dd>
          <dt>Email</dt><dd>{utente.email}</dd>
          <dt>Indirizzo</dt><dd>{utente.indirizzo}</dd>
          <dt>Sede</dt><dd>{utente.sede?.etichetta ?? '—'}</dd>
          <dt>Iscritto dal</dt><dd>{new Date(utente.createdAt).toLocaleDateString('it-IT')}</dd>
        </dl>
      </section>

      {lettore && riepilogo && (
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
            <p className="eyebrow">Prestiti in ritardo</p>
            <b>{riepilogo.prestitiInRitardo}</b>
          </article>
        </div>
      )}

      {lettore && libri && (
        <section className="blocco">
          <p className="eyebrow">La mia libreria</p>
          <dl className="dati">
            <dt>Salvati</dt><dd>{libri.length}</dd>
            <dt>Letti</dt><dd>{libri.filter((l) => l.letto).length}</dd>
          </dl>
        </section>
      )}
    </>
  )
}
