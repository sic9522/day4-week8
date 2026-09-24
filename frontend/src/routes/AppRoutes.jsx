import { useEffect } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { caricaProfilo } from '../store/authSlice'

import RottaProtetta from './RottaProtetta'
import LayoutCliente from '../layouts/LayoutCliente'
import LayoutConsole from '../layouts/LayoutConsole'

import Login from '../pages/Login'
import Registrazione from '../pages/Registrazione'
import Catalogo from '../pages/Catalogo'
import Preferiti from '../pages/Preferiti'
import MieiPrestiti from '../pages/MieiPrestiti'
import Profilo from '../pages/Profilo'
import Clienti from '../pages/console/Clienti'
import Libreria from '../pages/console/Libreria'
import PrestitiConsole from '../pages/console/Prestiti'
import Amministratori from '../pages/console/Amministratori'

const ADMIN = ['Admin', 'SuperUser']

// Chi entra va dove serve a lui: l'admin in console, il cliente in libreria
function Ingresso() {
  const utente = useSelector((s) => s.auth.utente)
  if (!utente) return <p className="caricamento corpo">Un attimo…</p>
  const admin = utente.ruoli.some((r) => ADMIN.includes(r))
  return <Navigate to={admin ? '/console/clienti' : '/catalogo'} replace />
}

export default function AppRoutes() {
  const dispatch = useDispatch()
  const { token, utente } = useSelector((s) => s.auth)

  // Il token sopravvive al ricaricamento della pagina, i dati dell'utente no: si richiedono
  useEffect(() => {
    if (token && !utente) dispatch(caricaProfilo())
  }, [token, utente, dispatch])

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/registrazione" element={<Registrazione />} />

        <Route element={<RottaProtetta />}>
          <Route path="/" element={<Ingresso />} />

          <Route element={<LayoutCliente />}>
            <Route path="/catalogo" element={<Catalogo />} />
            <Route path="/preferiti" element={<Preferiti />} />
            <Route path="/prestiti" element={<MieiPrestiti />} />
            <Route path="/profilo" element={<Profilo />} />
          </Route>
        </Route>

        <Route element={<RottaProtetta ruoli={ADMIN} />}>
          <Route element={<LayoutConsole />}>
            <Route path="/console/clienti" element={<Clienti />} />
            <Route path="/console/libreria" element={<Libreria />} />
            <Route path="/console/prestiti" element={<PrestitiConsole />} />
            <Route path="/console/profilo" element={<Profilo />} />
          </Route>
        </Route>

        <Route element={<RottaProtetta ruoli={['SuperUser']} />}>
          <Route element={<LayoutConsole />}>
            <Route path="/console/admin" element={<Amministratori />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
