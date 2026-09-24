import { useEffect, useRef, useState } from 'react'

function normalizza(s) {
  return String(s ?? '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '')
}

/**
 * La ricerca non salta al risultato: un raggio scorre i volumi uno a uno, quelli esclusi
 * si spengono, e alla fine chi corrisponde resta acceso. Il passo si adatta alla distanza,
 * così la scansione dura sempre circa un secondo e mezzo — sia che il libro sia il primo
 * dell'elenco, sia che sia l'ultimo.
 *
 * `elementi` sono gli oggetti da filtrare, `campi` dice dove cercare dentro ciascuno.
 * Il componente restituisce lo stato di ogni elemento al genitore, che lo passa alle card.
 */
export default function Ricerca({ elementi, campi, placeholder, onRisultati }) {
  const [testo, setTesto] = useState('')
  const [esito, setEsito] = useState(null)
  const timer = useRef(null)
  const lento = useRef(false)

  useEffect(() => {
    lento.current = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    return () => clearTimeout(timer.current)
  }, [])

  function ripulisci() {
    clearTimeout(timer.current)
    document.body.classList.remove('in-ricerca')
    onRisultati({ stati: {}, vinti: null })
    setEsito(null)
  }

  function cerca(e) {
    e.preventDefault()
    const q = normalizza(testo.trim())
    ripulisci()
    if (!q) return

    const buono = elementi.map((x) => campi.some((c) => normalizza(x[c]).includes(q)))
    const vinti = elementi.filter((_, k) => buono[k])

    let fino = elementi.length - 1
    for (let k = elementi.length - 1; k >= 0; k--) { if (buono[k]) { fino = k; break } }

    const passo = lento.current ? 0 : Math.max(50, Math.min(150, 1200 / (fino + 1)))
    document.body.classList.add('in-ricerca')
    setEsito({ testo: `Scorro ${elementi.length} volumi…` })

    const stati = {}
    let i = 0
    const passa = () => {
      if (i > 0) {
        const prec = elementi[i - 1]
        stati[prec.id] = buono[i - 1] ? 'trovato' : 'scartato'
      }
      if (i > fino) { concludi(); return }
      stati[elementi[i].id] = buono[i] ? 'trovato' : 'scansione'
      onRisultati({ stati: { ...stati }, vinti: null })
      i++
      timer.current = setTimeout(passa, i > fino ? (lento.current ? 0 : 420) : passo)
    }

    const concludi = () => {
      document.body.classList.remove('in-ricerca')
      if (vinti.length === 0) {
        setEsito({ testo: `Nessun risultato per «${testo.trim()}»` })
        timer.current = setTimeout(ripulisci, 1800)
        return
      }
      setEsito({ testo: `${vinti.length} ${vinti.length === 1 ? 'risultato' : 'risultati'} per `, forte: testo.trim() })
      onRisultati({ stati: {}, vinti })
    }

    passa()
  }

  return (
    <div>
      <form className="ricerca" role="search" onSubmit={cerca}>
        <input
          id="ricerca-testo"
          type="search"
          autoComplete="off"
          aria-label={placeholder}
          placeholder={placeholder}
          value={testo}
          onChange={(e) => setTesto(e.target.value)}
        />
        <button className="btn" type="submit">Cerca</button>
      </form>
      <p className="esito" role="status" aria-live="polite">
        {esito?.testo}
        {esito?.forte && <b>{esito.forte}</b>}
      </p>
    </div>
  )
}
