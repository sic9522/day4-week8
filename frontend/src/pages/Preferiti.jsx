import { useEffect, useMemo, useState } from 'react'
import Tessera from '../components/Tessera'
import { preferitiAPI, messaggioErrore } from '../services/endpoints'

const FILTRI = [
  { id: 'tutti', testo: 'Tutti' },
  { id: 'letti', testo: 'Letti' },
  { id: 'daleggere', testo: 'Da leggere' },
]

/** La libreria privata: i volumi col segnalibro, divisi fra letti e da leggere. */
export default function Preferiti() {
  const [preferiti, setPreferiti] = useState([])
  const [filtro, setFiltro] = useState('tutti')
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState(null)

  useEffect(() => {
    preferitiAPI.miei({ size: 100 })
      .then(({ data }) => setPreferiti(data.content))
      .catch((e) => setErrore(messaggioErrore(e, 'Non riesco a leggere la tua libreria')))
      .finally(() => setCaricamento(false))
  }, [])

  const mostrati = useMemo(() => preferiti.filter((p) => (
    filtro === 'tutti' || (filtro === 'letti' ? p.letto : !p.letto)
  )), [preferiti, filtro])

  async function commutaLetto(p) {
    try {
      const { data } = await preferitiAPI.segnaLetto(p.libro.id, !p.letto)
      setPreferiti((elenco) => elenco.map((x) => (x.id === data.id ? data : x)))
    } catch (e) {
      setErrore(messaggioErrore(e, 'Non riesco a segnare il libro'))
    }
  }

  async function togli(p) {
    try {
      await preferitiAPI.rimuovi(p.libro.id)
      setPreferiti((elenco) => elenco.filter((x) => x.id !== p.id))
    } catch (e) {
      setErrore(messaggioErrore(e, 'Non riesco a togliere il segnalibro'))
    }
  }

  if (caricamento) return <p className="caricamento">Un attimo…</p>

  return (
    <>
      <div className="barra">
        <div>
          <p className="eyebrow">Libreria privata</p>
          <h1>I miei libri</h1>
        </div>
      </div>

      <div className="filtri">
        {FILTRI.map((f) => (
          <button
            key={f.id}
            type="button"
            className={`filtro${filtro === f.id ? ' attivo' : ''}`}
            aria-pressed={filtro === f.id}
            onClick={() => setFiltro(f.id)}
          >
            {f.testo}
          </button>
        ))}
      </div>

      {errore && <p className="errore">{errore}</p>}

      {mostrati.length === 0 ? (
        <p className="vuoto">
          {filtro === 'tutti'
            ? 'La tua libreria è vuota. Metti il segnalibro su un volume del catalogo per trovarlo qui.'
            : 'Nessun libro in questo scaffale.'}
        </p>
      ) : (
        <div className="griglia cinque">
          {mostrati.map((p, i) => (
            <Tessera
              key={p.id}
              id={`pref-${p.id}`}
              indice={i + 1}
              titolo={p.libro.titolo}
              sotto={p.libro.autore}
              foto={p.libro.path}
              bollo={{ classe: p.letto ? 'vi' : 'si', testo: p.letto ? 'Letto' : 'Da leggere' }}
              segnalibro={{ messo: true, onCambia: () => togli(p) }}
              onApri={() => commutaLetto(p)}
            />
          ))}
        </div>
      )}

      {mostrati.length > 0 && (
        <p className="avviso" style={{ marginTop: 20 }}>
          Tocca una copertina per passarla fra letti e da leggere; il segnalibro la toglie dalla libreria.
        </p>
      )}
    </>
  )
}
