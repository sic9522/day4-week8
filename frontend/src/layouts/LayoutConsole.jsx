import { NavLink, Outlet } from 'react-router-dom'
import { useSelector } from 'react-redux'
import Testata from '../components/Testata'

/**
 * Il guscio di master e admin: barra laterale 1/5, corpo 4/5.
 * Le voci cambiano con il ruolo — il master governa gli admin, l'admin governa la sua sede.
 */
export default function LayoutConsole() {
  const ruoli = useSelector((s) => s.auth.utente?.ruoli ?? [])
  const master = ruoli.includes('SuperUser')

  const voci = master
    ? [
        { a: '/console/admin', testo: 'Admin' },
        { a: '/console/clienti', testo: 'Clienti' },
        { a: '/console/libreria', testo: 'Libreria' },
        { a: '/console/prestiti', testo: 'Prestiti' },
      ]
    : [
        { a: '/console/clienti', testo: 'Clienti' },
        { a: '/console/libreria', testo: 'Libreria' },
        { a: '/console/prestiti', testo: 'Prestiti' },
      ]

  const voce = ({ isActive }) => `voce${isActive ? ' attiva' : ''}`

  return (
    <>
      <Testata profiloA="/console/profilo" />
      <div className="guscio">
        <nav className="laterale" aria-label="Sezioni">
          <p className="eyebrow">{master ? 'Master' : 'Admin'}</p>
          {voci.map((v) => (
            <NavLink key={v.a} className={voce} to={v.a}>{v.testo}</NavLink>
          ))}
        </nav>
        <main className="corpo">
          <Outlet />
        </main>
      </div>
    </>
  )
}
