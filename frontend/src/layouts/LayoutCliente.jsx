import { NavLink, Outlet } from 'react-router-dom'
import Testata from '../components/Testata'

/** Il cliente entra direttamente nella libreria: niente barra laterale, solo tre schede. */
export default function LayoutCliente() {
  const scheda = ({ isActive }) => `scheda${isActive ? ' attiva' : ''}`
  return (
    <>
      <Testata />
      <nav className="schede" aria-label="Sezioni">
        <NavLink className={scheda} to="/catalogo">Libreria</NavLink>
        <NavLink className={scheda} to="/prestiti">I miei prestiti</NavLink>
        <NavLink className={scheda} to="/preferiti">Preferiti</NavLink>
      </nav>
      <main className="corpo">
        <Outlet />
      </main>
    </>
  )
}
