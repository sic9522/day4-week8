import { useEffect, useState } from 'react'
import Tessera, { tintaDi } from '../../components/Tessera'
import Ricerca from '../../components/Ricerca'
import LibroModale, { ScenaModale } from '../../components/LibroModale'
import { libriAPI, generiAPI, prestitiAPI, euro, dataIT, messaggioErrore } from '../../services/endpoints'

const VUOTO = {
  isbn: '', titolo: '', autore: '', edizione: '', casaEditrice: '', prezzo: '',
  annoDiUscita: '', copie: 1, copertinaRigida: false, pagine: '', descrizione: '', genereId: '', path: '',
}

export default function Libreria() {
  const [libri, setLibri] = useState([])
  const [generi, setGeneri] = useState([])
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState(null)
  const [aperto, setAperto] = useState(null)
  const [fuori, setFuori] = useState(null)
  const [nuovo, setNuovo] = useState(null)
  const [ricerca, setRicerca] = useState({ stati: {}, vinti: null })

  function carica() {
    return libriAPI.elenco({ size: 100 })
      .then(({ data }) => setLibri(data.content))
      .catch((e) => setErrore(messaggioErrore(e, 'Non riesco a leggere il catalogo')))
      .finally(() => setCaricamento(false))
  }

  useEffect(() => {
    carica()
    generiAPI.elenco().then(({ data }) => setGeneri(data)).catch(() => {})
  }, [])

  // Aprendo un libro l'admin vuole sapere chi ha le copie e quando tornano
  useEffect(() => {
    if (!aperto) return
    prestitiAPI.perLibro(aperto.id)
      .then(({ data }) => setFuori(data))
      .catch(() => setFuori([]))
  }, [aperto])

  const mostrati = ricerca.vinti ?? libri

  if (caricamento) return <p className="caricamento">Un attimo…</p>

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

      <div className="griglia cinque">
        {mostrati.map((l, i) => (
          <Tessera
            key={l.id}
            id={`lib-${l.id}`}
            indice={i + 2}
            titolo={l.titolo}
            sotto={l.autore}
            foto={l.path}
            stato={ricerca.stati[l.id]}
            bollo={{
              classe: l.copieDisponibili > 0 ? 'si' : 'no',
              testo: l.copieDisponibili > 0 ? `${l.copieDisponibili} di ${l.copieTotali}` : 'Tutte fuori',
            }}
            onApri={() => setAperto(l)}
          />
        ))}

        <button
          className="tessera nuova"
          type="button"
          onClick={() => setNuovo({ ...VUOTO, genereId: generi[0]?.id ?? '' })}
        >
          <span className="piu">+</span>
          <span className="che">Nuovo libro</span>
        </button>
      </div>

      <ScenaModale>
        {aperto && (
          <LibroModale
            key={aperto.id}
            id={`lib-${aperto.id}`}
            onChiudi={() => { setAperto(null); setFuori(null) }}
            copertina={{
              titolo: aperto.titolo,
              sotto: aperto.autore,
              foto: aperto.path,
              tinta: tintaDi(libri.indexOf(aperto) + 2),
              bollo: {
                classe: aperto.copieDisponibili > 0 ? 'si' : 'no',
                testo: aperto.copieDisponibili > 0 ? 'Disponibile' : 'Tutte fuori',
              },
            }}
            exlibris={{
              titolo: 'Scheda di catalogo',
              dati: [
                ['Editore', aperto.casaEditrice],
                ['Edizione', aperto.edizione],
                ['Anno', aperto.annoDiUscita],
                ['Pagine', aperto.pagine],
                ['ISBN', aperto.isbn],
              ],
            }}
          >
            <div>
              <h2>{aperto.titolo}</h2>
              <p className="contatto">{aperto.autore} · {aperto.genere}</p>
            </div>

            {aperto.descrizione && (
              <div className="sezione">
                <p className="eyebrow">Sinossi</p>
                <p className="sinossi">{aperto.descrizione}</p>
              </div>
            )}

            <div className="sezione">
              <p className="eyebrow">{fuori?.length === 1 ? 'La copia fuori' : 'Le copie fuori'}</p>
              {!fuori ? <p className="vuoto">Un attimo…</p>
                : fuori.length === 0 ? <p className="vuoto">Tutte le copie sono a scaffale.</p>
                  : (
                    <>
                      {fuori.slice(0, 4).map((p) => (
                        <div key={p.id} className={`riga${p.stato === 'IN_RITARDO' ? ' tardi' : ''}`}>
                          <span className="tit">{p.user.nome} {p.user.cognome}</span>
                          <span className="quando">{dataIT(p.dataRiconsegnaPrevista)}</span>
                        </div>
                      ))}
                      {fuori.length > 4 && <p className="ancora">e altre {fuori.length - 4}</p>}
                    </>
                  )}
            </div>

            <div className="sezione">
              <p className="eyebrow">Copie</p>
              <dl className="dati">
                <dt>Disponibili</dt><dd>{aperto.copieDisponibili} di {aperto.copieTotali}</dd>
                <dt>Prezzo di copertina</dt><dd>{euro(aperto.prezzo)}</dd>
                <dt>Rilegatura</dt><dd>{aperto.copertinaRigida ? 'Rigida' : 'Brossura'}</dd>
              </dl>
            </div>

            <div className="azioni">
              <button
                className="btn piano"
                type="button"
                onClick={async () => {
                  try {
                    await libriAPI.aggiungiCopie({ idLibro: aperto.id, copie: 1 })
                    setAperto(null)
                    carica()
                  } catch (e) {
                    setErrore(messaggioErrore(e, 'Non riesco ad aggiungere la copia'))
                  }
                }}
              >
                Aggiungi una copia
              </button>
            </div>
          </LibroModale>
        )}

        {nuovo && (
          <ModuloLibro
            dati={nuovo}
            generi={generi}
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

function ModuloLibro({ dati, generi, onCambia, onChiudi, onCreato, onErrore }) {
  const campo = (k) => (e) => onCambia({ ...dati, [k]: e.target.value })

  async function crea() {
    try {
      await libriAPI.nuovo({
        ...dati,
        isbn: Number(dati.isbn),
        prezzo: Number(dati.prezzo),
        annoDiUscita: Number(dati.annoDiUscita),
        copie: Number(dati.copie),
        pagine: dati.pagine ? Number(dati.pagine) : null,
        copertinaRigida: Boolean(dati.copertinaRigida),
      })
      onCreato()
    } catch (e) {
      onErrore(messaggioErrore(e, 'Non riesco a creare il libro'))
    }
  }

  return (
    <LibroModale
      id="nuovo-libro"
      onChiudi={onChiudi}
      copertina={{
        titolo: dati.titolo || 'Nuovo libro',
        sotto: dati.autore || 'A catalogo',
        tinta: 'linear-gradient(155deg, #262240 0%, #15121F 64%, #0A0718 100%)',
        bollo: { classe: 'vi', testo: 'Bozza' },
      }}
      exlibris={{ dati: [['Copie', dati.copie], ['Genere', generi.find((g) => g.id === dati.genereId)?.nome]] }}
    >
      <div>
        <h2>Aggiungi al catalogo</h2>
        <p className="contatto">La copertina può arrivare da Open Library: incolla l&apos;indirizzo dell&apos;immagine.</p>
      </div>

      <div className="sezione modulo-fitto due">
        <label className="campo" htmlFor="nl-isbn">ISBN<input id="nl-isbn" inputMode="numeric" value={dati.isbn} onChange={campo('isbn')} /></label>
        <label className="campo" htmlFor="nl-titolo">Titolo<input id="nl-titolo" value={dati.titolo} onChange={campo('titolo')} /></label>
        <label className="campo" htmlFor="nl-autore">Autore<input id="nl-autore" value={dati.autore} onChange={campo('autore')} /></label>
        <label className="campo" htmlFor="nl-editore">Casa editrice<input id="nl-editore" value={dati.casaEditrice} onChange={campo('casaEditrice')} /></label>
        <div className="coppia">
          <label className="campo" htmlFor="nl-anno">Anno<input id="nl-anno" inputMode="numeric" value={dati.annoDiUscita} onChange={campo('annoDiUscita')} /></label>
          <label className="campo" htmlFor="nl-pagine">Pagine<input id="nl-pagine" inputMode="numeric" value={dati.pagine} onChange={campo('pagine')} /></label>
        </div>
        <div className="coppia">
          <label className="campo" htmlFor="nl-prezzo">Prezzo di copertina<input id="nl-prezzo" inputMode="decimal" value={dati.prezzo} onChange={campo('prezzo')} /></label>
          <label className="campo" htmlFor="nl-copie">Copie<input id="nl-copie" type="number" min={1} value={dati.copie} onChange={campo('copie')} /></label>
        </div>
        <label className="campo" htmlFor="nl-genere">Genere
          <select id="nl-genere" value={dati.genereId} onChange={campo('genereId')}>
            {generi.map((g) => <option key={g.id} value={g.id}>{g.nome}</option>)}
          </select>
        </label>
        <label className="campo" htmlFor="nl-path">Copertina (indirizzo)<input id="nl-path" value={dati.path} onChange={campo('path')} /></label>
        <label className="campo" htmlFor="nl-descrizione">Sinossi<textarea id="nl-descrizione" rows={3} value={dati.descrizione} onChange={campo('descrizione')} /></label>
      </div>

      <div className="azioni">
        <button className="btn" type="button" onClick={crea}>Aggiungi al catalogo</button>
      </div>
    </LibroModale>
  )
}
