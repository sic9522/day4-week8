import { useEffect, useState } from 'react'
import { AnimatePresence, motion } from 'motion/react'

/**
 * La modale a forma di libro, usata per tutto: schede libro, clienti, admin, moduli.
 *
 * Il volo dalla card al centro dello schermo lo fa Motion con `layoutId`: la card di
 * partenza e la copertina qui condividono lo stesso id, e Motion interpola fra le due
 * posizioni da solo. Finito il volo si aggiunge la classe `aperto` e la copertina ruota
 * sul cardine sinistro, scoprendo la pagina.
 */
export default function LibroModale({ id, copertina, exlibris, children, onChiudi }) {
  const [aperto, setAperto] = useState(false)

  useEffect(() => {
    const t = setTimeout(() => setAperto(true), 420)
    return () => clearTimeout(t)
  }, [])

  useEffect(() => {
    const esc = (e) => { if (e.key === 'Escape') onChiudi() }
    document.addEventListener('keydown', esc)
    return () => document.removeEventListener('keydown', esc)
  }, [onChiudi])

  return (
    <motion.div
      className="scena"
      role="dialog"
      aria-modal="true"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.3 }}
      onClick={(e) => { if (e.target === e.currentTarget) onChiudi() }}
    >
      <button className="chiudi" type="button" onClick={onChiudi} autoFocus>
        Chiudi · Esc
      </button>

      <motion.div className="culla" layoutId={id} transition={{ duration: 0.55, ease: [0.22, 0.85, 0.25, 1] }}>
        <div className={`volume${aperto ? ' aperto' : ''}`}>
          <div className="pagina">{children}</div>

          <div className="cardine">
            <div className={`faccia copertina${copertina.foto ? ' con-foto' : ''}`} style={{ background: copertina.tinta }}>
              {copertina.foto && <img className="copertina-foto" src={copertina.foto} alt="" />}
              {copertina.bollo && <span className={`bollo ${copertina.bollo.classe}`}>{copertina.bollo.testo}</span>}
              <span className="nome">{copertina.titolo}</span>
              <span className="matricola">{copertina.sotto}</span>
            </div>

            <div className="faccia exlibris">
              <div className="targa">
                <p className="titolo-targa">{exlibris.titolo ?? 'Ex libris'}</p>
                {exlibris.bollo && <span className={`bollo ${exlibris.bollo.classe}`}>{exlibris.bollo.testo}</span>}
              </div>
              <dl className="dati">
                {exlibris.dati.map(([chiave, valore]) => (
                  <div key={chiave} style={{ display: 'contents' }}>
                    <dt>{chiave}</dt>
                    <dd>{valore ?? '—'}</dd>
                  </div>
                ))}
              </dl>
            </div>
          </div>
        </div>
      </motion.div>
    </motion.div>
  )
}

// Involucro che gestisce l'uscita: senza AnimatePresence la modale sparirebbe di colpo
export function ScenaModale({ children }) {
  return <AnimatePresence>{children}</AnimatePresence>
}
