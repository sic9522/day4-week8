import { motion } from 'motion/react'

const TINTE = [
  ['#2C1B5E', '#120B2B'], ['#123C42', '#0A1F26'], ['#4A1C2E', '#250D18'],
  ['#1B3358', '#0B1A2E'], ['#3D2A12', '#1E1408'], ['#2A1A3F', '#150A22'],
  ['#14342B', '#081A14'], ['#442036', '#22101B'],
]

// La tinta non è casuale: dipende dall'indice, così un volume ha sempre lo stesso colore
export function tintaDi(indice) {
  const [a, b] = TINTE[indice % TINTE.length]
  return `linear-gradient(155deg, ${a} 0%, ${b} 64%, #0A0718 100%)`
}

/** Un volume nella griglia: copertina, bollo di stato e, per i clienti, il segnalibro. */
export default function Tessera({ id, indice = 0, titolo, sotto, bollo, foto, stato, onApri, segnalibro }) {
  return (
    <motion.button
      type="button"
      layoutId={id}
      className={`tessera${foto ? ' con-foto' : ''}${stato ? ` ${stato}` : ''}`}
      style={{ background: tintaDi(indice) }}
      onClick={onApri}
      aria-label={`${titolo}${sotto ? `, ${sotto}` : ''}. Apri la scheda.`}
    >
      {foto && <img className="copertina-foto" src={foto} alt="" />}

      {segnalibro && (
        <span
          className="segnalibro"
          role="button"
          tabIndex={0}
          aria-pressed={segnalibro.messo}
          aria-label={`${segnalibro.messo ? 'Togli' : 'Metti'} il segnalibro su ${titolo}`}
          onClick={(e) => { e.stopPropagation(); segnalibro.onCambia() }}
          onKeyDown={(e) => {
            if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); e.stopPropagation(); segnalibro.onCambia() }
          }}
        />
      )}

      {bollo && <span className={`bollo ${bollo.classe}`}>{bollo.testo}</span>}
      <span className="nome">{titolo}</span>
      {sotto && <span className="matricola">{sotto}</span>}
    </motion.button>
  )
}
