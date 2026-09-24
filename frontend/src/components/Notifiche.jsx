import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { notificheAPI, messaggioErrore } from '../services/endpoints'

/**
 * La campanella con il numero di avvisi non letti.
 *
 * Il conteggio si ricontrolla ogni trenta secondi. È volutamente un sondaggio e non
 * un WebSocket: per un avviso che può arrivare con mezzo minuto di ritardo, tenere
 * aperta una connessione per ogni utente costa più di quanto renda.
 */
export default function Notifiche() {
  const vai = useNavigate()
  const [quante, setQuante] = useState(0)
  const [aperto, setAperto] = useState(false)
  const [elenco, setElenco] = useState([])
  const [errore, setErrore] = useState(null)
  const timer = useRef(null)

  const conta = useCallback(() => {
    notificheAPI.daLeggere()
      .then(({ data }) => setQuante(data.quante))
      .catch(() => {})
  }, [])

  useEffect(() => {
    conta()
    timer.current = setInterval(conta, 30000)
    return () => clearInterval(timer.current)
  }, [conta])

  async function apri() {
    const prossimo = !aperto
    setAperto(prossimo)
    if (!prossimo) return
    try {
      const { data } = await notificheAPI.mie({ size: 30 })
      setElenco(data.content)
      await notificheAPI.lette()
      setQuante(0)
    } catch (e) {
      setErrore(messaggioErrore(e, 'Non riesco a leggere gli avvisi'))
    }
  }

  return (
    <div className="notifiche">
      <button
        className="campanella"
        type="button"
        onClick={apri}
        aria-expanded={aperto}
        aria-label={quante ? `${quante} avvisi da leggere` : 'Avvisi'}
      >
        Avvisi
        {quante > 0 && <span className="conteggio">{quante}</span>}
      </button>

      {aperto && (
        <div className="cassetto">
          {errore && <p className="avviso male">{errore}</p>}
          {elenco.length === 0 ? (
            <p className="vuoto">Nessun avviso.</p>
          ) : elenco.map((n) => (
            <button
              key={n.id}
              type="button"
              className={`avvisoriga${n.letta ? '' : ' nuova'}`}
              onClick={() => { setAperto(false); vai(n.destinazione) }}
            >
              <span>{n.testo}</span>
              <time dateTime={n.createdAt}>
                {new Date(n.createdAt).toLocaleString('it-IT', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })}
              </time>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
