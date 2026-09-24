import { useEffect, useMemo, useState } from 'react'
import Tessera, { tintaDi } from '../components/Tessera'
import Ricerca from '../components/Ricerca'
import LibroModale, { ScenaModale } from '../components/LibroModale'
import { libriAPI, preferitiAPI, costantiAPI, richiesteAPI, euro, messaggioErrore } from '../services/endpoints'

export default function Catalogo() {
  const [libri, setLibri] = useState([])
  const [preferiti, setPreferiti] = useState([])
  const [tariffe, setTariffe] = useState(null)
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState(null)
  const [aperto, setAperto] = useState(null)
  const [ricerca, setRicerca] = useState({ stati: {}, vinti: null })
  const [chiesto, setChiesto] = useState(null)
  const [durata, setDurata] = useState('MEDIA')

  useEffect(() => {
    Promise.all([libriAPI.elenco({ size: 60 }), preferitiAPI.miei({ size: 100 }), costantiAPI.tariffe()])
      .then(([l, p, t]) => {
        setLibri(l.data.content)
        setPreferiti(p.data.content)
        setTariffe(t.data)
      })
      .catch((e) => setErrore(messaggioErrore(e, 'Non riesco a leggere il catalogo')))
      .finally(() => setCaricamento(false))
  }, [])

  const salvati = useMemo(() => new Set(preferiti.map((p) => p.libro.id)), [preferiti])

  async function commutaSegnalibro(libro) {
    try {
      if (salvati.has(libro.id)) {
        await preferitiAPI.rimuovi(libro.id)
        setPreferiti((p) => p.filter((x) => x.libro.id !== libro.id))
      } else {
        const { data } = await preferitiAPI.aggiungi(libro.id)
        setPreferiti((p) => [...p, data])
      }
    } catch (e) {
      setErrore(messaggioErrore(e, 'Non riesco ad aggiornare i preferiti'))
    }
  }

  const mostrati = ricerca.vinti ?? libri

  if (caricamento) return <p className="caricamento">Sto aprendo gli scaffali…</p>

  return (
    <>
      <div className="barra">
        <div>
          <p className="eyebrow">Catalogo</p>
          <h1>Libreria</h1>
        </div>
        <Ricerca
          elementi={libri}
          campi={['titolo', 'autore', 'genere', 'casaEditrice', 'annoDiUscita']}
          placeholder="Titolo, autore, genere o anno"
          onRisultati={setRicerca}
        />
      </div>

      {errore && <p className="errore">{errore}</p>}

      {mostrati.length === 0 ? (
        <p className="vuoto">Nessun libro nel catalogo.</p>
      ) : (
        <div className="griglia">
          {mostrati.map((libro, i) => (
            <Tessera
              key={libro.id}
              id={`libro-${libro.id}`}
              indice={i}
              titolo={libro.titolo}
              sotto={libro.autore}
              foto={libro.path}
              stato={ricerca.stati[libro.id]}
              bollo={{
                classe: libro.copieDisponibili > 0 ? 'si' : 'no',
                testo: libro.copieDisponibili > 0 ? `${libro.copieDisponibili} copie` : 'In prestito',
              }}
              segnalibro={{ messo: salvati.has(libro.id), onCambia: () => commutaSegnalibro(libro) }}
              onApri={() => setAperto(libro)}
            />
          ))}
        </div>
      )}

      <ScenaModale>
        {aperto && (
          <LibroModale
            key={aperto.id}
            id={`libro-${aperto.id}`}
            onChiudi={() => setAperto(null)}
            copertina={{
              titolo: aperto.titolo,
              sotto: aperto.autore,
              foto: aperto.path,
              tinta: tintaDi(libri.indexOf(aperto)),
              bollo: {
                classe: aperto.copieDisponibili > 0 ? 'si' : 'no',
                testo: aperto.copieDisponibili > 0 ? 'Disponibile' : 'In prestito',
              },
            }}
            exlibris={{
              titolo: 'Scheda di catalogo',
              bollo: {
                classe: aperto.copieDisponibili > 0 ? 'si' : 'no',
                testo: `${aperto.copieDisponibili} di ${aperto.copieTotali}`,
              },
              dati: [
                ['Editore', aperto.casaEditrice],
                ['Anno', aperto.annoDiUscita],
                ['Pagine', aperto.pagine],
                ['Genere', aperto.genere],
                ['Rilegatura', aperto.copertinaRigida ? 'Rigida' : 'Brossura'],
                ['ISBN', aperto.isbn],
              ],
            }}
          >
            <div>
              <h2>{aperto.titolo}</h2>
              <p className="contatto">{aperto.autore}</p>
            </div>

            {aperto.descrizione && (
              <div className="sezione">
                <p className="eyebrow">Sinossi</p>
                <p className="sinossi">{aperto.descrizione}</p>
              </div>
            )}

            {tariffe && (
              <div className="sezione">
                <p className="eyebrow">Fattelo arrivare a casa</p>
                <div className="fasce">
                  {tariffe.fasce.map((f) => (
                    <label className="fascia" key={f.durata}>
                      <input
                        type="radio"
                        name="durata-richiesta"
                        value={f.durata}
                        checked={durata === f.durata}
                        onChange={() => setDurata(f.durata)}
                      />
                      <span>{f.giorni} giorni</span>
                      <span className="prezzo">{euro(f.costo)}</span>
                    </label>
                  ))}
                </div>
                <p className="sinossi" style={{ marginTop: 8 }}>
                  Oltre la scadenza si aggiungono {euro(tariffe.penaleGiornaliera)} al giorno,
                  fino a {euro(tariffe.penaleMassima)}.
                </p>
              </div>
            )}

            <div className="azioni">
              <button
                className="btn"
                type="button"
                disabled={aperto.copieDisponibili === 0 || chiesto === aperto.id}
                onClick={async () => {
                  try {
                    await richiesteAPI.apri({ libroId: aperto.id, durata })
                    setChiesto(aperto.id)
                  } catch (e) {
                    setErrore(messaggioErrore(e, 'Non riesco a mandare la richiesta'))
                  }
                }}
              >
                {chiesto === aperto.id ? 'Richiesta inviata' : 'Chiedila a casa'}
              </button>
              <button
                className={`btn${salvati.has(aperto.id) ? '' : ' piano'}`}
                type="button"
                onClick={() => commutaSegnalibro(aperto)}
              >
                {salvati.has(aperto.id) ? 'Togli il segnalibro' : 'Metti il segnalibro'}
              </button>
            </div>

            <p className="avviso">
              {aperto.copieDisponibili === 0
                ? 'Tutte le copie sono fuori: riprova quando ne rientra una.'
                : 'La spedizione parte quando il tuo bibliotecario approva la richiesta.'}
            </p>
          </LibroModale>
        )}
      </ScenaModale>
    </>
  )
}
